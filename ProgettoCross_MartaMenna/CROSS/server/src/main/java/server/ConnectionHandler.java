package server;

import com.google.gson.Gson;

import common.Request;
import common.ConfigLoader;
import server.OrderTypes.Order;
import server.OrderTypes.MarketOrder;
import server.OrderTypes.StopOrder;
import server.OrderTypes.LimitOrder;

import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.LinkedList;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ConnectionHandler implements Runnable {
    
    private final SelectionKey key;
    private final InetSocketAddress address;
    private final SocketChannel channel;
    private final ByteBuffer readBuffer;
    private final ByteBuffer writeBuffer;
    // Coda dei messaggi da inviare sul canale
    private final Queue<byte[]> messageQueue;
    // Istanza Gson per operazioni di streaming
    private final Gson gson;
    // Riferimenti ai manager condivisi
    private final UserManager userManager;
    private final OrderBookManager orderBookManager;
    private final UDPNotificationSender udpNotificationSender;
    private final NotificationService notificationService;

    /**
     * Costruisce un nuovo gestore delle connessioni client che processa richieste JSON.
     * Gestisce le operazioni di I/O non bloccanti e il processing dei messaggi per una singola connessione client.
     *
     * @param key la SelectionKey associata al canale del client
     * @param userManager il gestore per autenticazione e gestione degli utenti
     * @param orderBookManager il gestore degli ordini
     * @param notificationService il servizio per l'invio delle notifiche di prezzo
     * @param config il caricatore della configurazione per le dimensioni dei buffer
     *
     * <p>Funzionalità principali:</p>
     * <ul>
     *   <li>Lettura dei messaggi JSON dal client</li>
     *   <li>Processing delle richieste (registrazione, login, ordini, etc.)</li>
     *   <li>Scrittura delle risposte JSON</li>
     * </ul>
     *
     * <p>Supporta le seguenti operazioni:</p>
     * <ul>
     *   <li>Gestione utenti: register, login, logout, updateCredentials</li>
     *   <li>Gestione ordini: limitOrder, marketOrder, stopOrder, cancelOrder</li>
     *   <li>Query: getPriceHistory</li>
     * </ul>
     */
    public ConnectionHandler(SelectionKey key,
                             InetSocketAddress address,
                             UserManager userManager,
                             OrderBookManager orderBookManager,
                             UDPNotificationSender udpNotificationSender,
                             NotificationService notificationService,
                             ConfigLoader config) {
        this.key = key;
        this.address = address;
        this.channel = (SocketChannel) key.channel();
        this.readBuffer = ByteBuffer.allocate(config.getIntProperty("writeBufferSize"));
        this.writeBuffer = ByteBuffer.allocate(config.getIntProperty("readBufferSize"));
        this.messageQueue = new LinkedList<>();
        this.gson = new Gson();

        this.userManager = userManager;
        this.orderBookManager = orderBookManager;
        this.udpNotificationSender = udpNotificationSender;
        this.notificationService = notificationService;
    }
    
    @Override
    public void run() {
        try {
            if (key.isReadable())
                handleRead();
            
            if (key.isWritable())
                handleWrite();
        
        } catch (IOException e) {
            System.err.println(channel + " Errore nella gestione della connessione: " + e.getMessage());
            closeChannel();
        }
    }
    
    /// Legge dal canale e processa il messaggio ricevuto.
    private synchronized void handleRead() throws IOException {
        // Clears this buffer. The position is set to zero, the limit is set to the capacity, and the mark is discarded.
        readBuffer.clear();
        // Legge i dati dal canale e li memorizza nel buffer
        int bytesRead = channel.read(readBuffer);
        
        // Se il client ha chiuso la connessione
        if (bytesRead == -1) {
            Channel channel = key.channel();
            // rimuove il canale a cui è associato key.cancel() dal Selector
            key.cancel();
            try {
                channel.close();
            } catch (IOException ex) {
                System.out.println("Errore nella chiusura del canale" + ex.getMessage());
            }
            return;
        }
        
        // Setta buffer per la lettura    
        readBuffer.flip();
        byte[] data = new byte[readBuffer.remaining()];
        // Legge i dati dal buffer e li memorizza nell'array data
        readBuffer.get(data);

        String receivedMsg = new String(data, StandardCharsets.UTF_8);
        System.out.println(channel + " Messaggio ricevuto: " + receivedMsg);
        
        // Processa il messaggio JSON e genera una risposta.
        byte[] response = handleRequest(receivedMsg);
        //Se la risposta non è nulla la aggiunge alla coda
        if (response != null) {
            queueMessage(response);
        }
    }
    
    /// Aggiunge un messaggio alla coda per la scrittura sul canale.
    public void queueMessage(byte[] message) {
        messageQueue.add(message);
        // Abilita l'interesse per la scrittura sul canale
        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        
        key.selector().wakeup();
    }
    
    /// Scrive sul canale
    private synchronized void handleWrite() throws IOException {
        while (!messageQueue.isEmpty()) {
            // Non rimuoviamo subito il messaggio
            byte[] dataToWrite = messageQueue.peek();

            // se il messaggio è piu grande del writeBuffer
            if (writeBuffer.capacity() < dataToWrite.length) {
                System.err.println("> Messaggio troppo grande per il buffer");
                messageQueue.poll(); // Rimuoviamo il messaggio problematico
                return;
            }
            
            // Scrivi il messaggio sul WriteBuffer
            writeBuffer.clear();
            writeBuffer.put(dataToWrite);
            writeBuffer.flip();
            
            // Invia il messaggio sul canale
            while (writeBuffer.hasRemaining()) {
                channel.write(writeBuffer);
            }
            
            System.out.println(channel + " Messaggio inviato: " + new String(dataToWrite, StandardCharsets.UTF_8));
            //Rimuove il messaggio dalla testa (FIFO)
            messageQueue.poll();
        }
        // Disabilita l'interesse per la scrittura sul canale
        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
    }
    
    /**
     * Processa il messaggio JSON ricevuto.
     * Restituisce una stringa JSON di risposta.
     */
    private byte[] handleRequest(String requestStr) {
        //Dichiara il byteArray per la risposta
        byte[] responseBytes;
        
        //Legge il JSON e lo converte in un oggetto del tipo Request
        Request request = gson.fromJson(requestStr, Request.getType());
        if (request == null)
            return null;
        //Estrae l'operazione da eseguire
        String operation = request.getOperation();
        //Estrae i valori della richiesta
        Map<String, Object> values = request.getValues();
        
        switch (operation) {
            // case "register":
            //     response = handleRegister(values);
            //     break;
            case "login":
                responseBytes = serializeMessage(handleLogin(values));
                break;
            case "logout":
                responseBytes = serializeMessage(handleLogout(values));
                break;
            case "updateCredentials":
                responseBytes = serializeMessage(handleUpdateCredentials(values));
                break;
            case "insertLimitOrder":
                responseBytes = serializeMessage(handleInsertLimitOrder(values));
                break;
            case "insertMarketOrder":
                responseBytes = serializeMessage(handleInsertMarketOrder(values));
                break;
            case "insertStopOrder":
                responseBytes = serializeMessage(handleInsertStopOrder(values));
                break;
            case "cancelOrder":
                responseBytes = serializeMessage(handleCancelOrder(values));
                break;
            case "getPriceHistory":
                responseBytes = serializeList(handleGetPriceHistory(values));
                break;
            default:
                responseBytes = serializeMessage(createErrorResponse(103, "Operazione sconosciuta"));
                break;
        }
        return responseBytes;
    }
    
    private byte[] serializeList(List<DailyPrice> list) {
        if (list == null)
            return serializeMessage(createErrorResponse(101, "parametri anno-mese sbagliati o inesistenti"));
        String json = gson.toJson(list);
        return json.getBytes(StandardCharsets.UTF_8);
    }
    
    private byte[] serializeMessage(Map<String, Object> messageMap) {
    return gson.toJson(messageMap).getBytes(StandardCharsets.UTF_8);
    }
    
    // private Map<String, Object> handleRegister(Map<String, Object> values) {
    //     String username = (String) values.get("username");
    //     String password = (String) values.get("password");
    //     boolean success = userManager.registerUser(username, password);
    //     if (success) {
    //         return createSuccessResponse();
    //     } else {
    //         if (password == null || password.isEmpty())
    //             return createErrorResponse(101, "Password non valida");
    //         else
    //             return createErrorResponse(102, "Username non disponibile");
    //     }
    // }
    
    // Metodi di supporto per le operazioni

    private Map<String, Object> handleLogin(Map<String, Object> values) {
        String username = (String) values.get("username");
        String password = (String) values.get("password");
        int threshold = ((Number) values.get("threshold")).intValue();
        
        if (userManager.isLogged(username))
            return createErrorResponse(102, "Utente già loggato");
        
        boolean success = userManager.loginUser(username, password);
        if (success) {
            notificationService.registerMulticastPreference(threshold);
            return createSuccessResponse();
        } else {
            return createErrorResponse(101, "Credenziali errate");
        }
    }
    
    private Map<String, Object> handleLogout(Map<String, Object> values) {
        String username = (String) values.get("username");
        boolean success = userManager.logoutUser(username);
        if (success)
            return createSuccessResponse();
        else
            return createErrorResponse(101, "Logout fallito: utente non loggato");
    }
    
    private Map<String, Object> handleUpdateCredentials(Map<String, Object> values) {
        String username = (String) values.get("username");
        String oldPassword = (String) values.get("old_password");
        String newPassword = (String) values.get("new_password");
        
        if(oldPassword.equals(newPassword))
            return createErrorResponse(102, "Nuova password uguale alla vecchia");
        
        if (userManager.isLogged(username))
            return createErrorResponse(104, "Utente attualmente loggato");
        
        boolean success = userManager.updatePassword(username, oldPassword, newPassword);
        if (success)
            return createSuccessResponse();
        else
            return createErrorResponse(101, "username/vecchia password non coincidono o non esiste " +
                    "questo username");
    }
    
    private Map<String, Object> handleInsertLimitOrder(Map<String, Object> values) {
        // recupera i valori
        String type = (String) values.get("type");
        int size = ((Number) values.get("size")).intValue();
        int price = ((Number) values.get("price")).intValue();

        // crea e aggiungi all'orderBookManager il nuovo ordine
        Order order = new LimitOrder(type, size, price);
        int orderId = orderBookManager.insertOrder(order);

        // aggiungi la coppia orderid - socket per notifiche future sull'erogazione dell'ordine
        udpNotificationSender.add(orderId, address);

        // ritorna la risposta
        return createOrderResponse(orderId);
    }
    
    private Map<String, Object> handleInsertMarketOrder(Map<String, Object> values) {
        String type = (String) values.get("type");
        int size = ((Number) values.get("size")).intValue();

        Order order = new MarketOrder(type, size, 0);
        int orderId = orderBookManager.insertOrder(order);

        udpNotificationSender.add(orderId, address);

        return createOrderResponse(orderId);
    }
    
    private Map<String, Object> handleInsertStopOrder(Map<String, Object> values) {
        String type = (String) values.get("type");
        int size = ((Number) values.get("size")).intValue();
        int price = ((Number) values.get("price")).intValue();

        Order order = new StopOrder(type, size, price);
        int orderId = orderBookManager.insertOrder(order);

        udpNotificationSender.add(orderId, address);

        return createOrderResponse(orderId);
    }
    
    private Map<String, Object> handleCancelOrder(Map<String, Object> values) {
        int orderId = ((Number) values.get("orderId")).intValue();
        boolean success = orderBookManager.cancelOrder(orderId);
        if (success) {
            udpNotificationSender.remove(orderId);
            return createSuccessResponse();
        }
        else
            return createErrorResponse(101, "Ordine inesistente o non cancellabile");
    }
    
    private List<DailyPrice> handleGetPriceHistory(Map<String, Object> values) {
        int month = ((Number) values.get("month")).intValue();
        int year = ((Number) values.get("year")).intValue();
        List<DailyPrice> response = orderBookManager.getPriceHistory(year, month);
        return response;
    }
    
    /// Metodi di utilità per generare risposte
    private Map<String, Object> createSuccessResponse() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("response", 100);
        resp.put("message", "OK");
        return resp;
    }
    
    private Map<String, Object> createOrderResponse(int orderId) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("orderId", orderId);
        return resp;
    }
    
    private Map<String, Object> createErrorResponse(int code, String message) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("response", code);
        resp.put("errorMessage", message);
        return resp;
    }

    ///Chiude il canale e rimuove la chiave dal selector.
    private void closeChannel() {
        try {
            key.cancel();
            channel.close();
            System.out.println("Connessione chiusa.");
        } catch (IOException e) {
            System.out.println("Errore nella chiusura del canale" + e.getMessage());
        }
    }
}