package client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import common.ConfigLoader;
import common.RegistrationResponseDTO;
import common.Request;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Scanner;

public class ConnectionHandler implements Runnable {

    private final SocketChannel socketChannel;
    private final RegistrationClient registrationClient;

    private final int readBuffSize;
    private final int writeBuffSize;
    private final ByteBuffer writeBuffer;
    private final ByteBuffer readBuffer;
    private final int udpReadBuffSize;

    //parametri utili per la connessione al gruppo multicast
    private final int serverUdpPort;
    private final String multicastAddress;

    private MulticastNotificationReceiver receiver;
    private Thread receiverThread;

    private final Gson gson;
    private boolean isGetPriceHistory;
    private boolean quitFlag;

    public ConnectionHandler(SocketChannel socketChannel, RegistrationClient regClient, ConfigLoader config) {
        this.socketChannel = socketChannel;
        this.registrationClient = regClient;

        // bytebuffer utili per la scrittura e lettura sul canale
        this.writeBuffSize = config.getIntProperty("writeBuffSize");
        this.readBuffSize = config.getIntProperty("readBuffSize");
        this.writeBuffer = ByteBuffer.allocate(writeBuffSize);
        this.readBuffer = ByteBuffer.allocate(readBuffSize);
        this.udpReadBuffSize = config.getIntProperty("udpReadBuffSize");

        this.serverUdpPort = config.getIntProperty("serverUdpPort");
        this.multicastAddress = config.getProperty("multicastAddress");

        this.gson = new Gson();
        this.isGetPriceHistory = false;
        this.quitFlag = false;
    }

    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            nonAuthenticatedLoop(scanner, null);
        }
    }

    public void nonAuthenticatedLoop(Scanner scanner, String username) {
        int op;
        // questo ciclo contiene le uniche operazioni possibili senza essere autenticati
        while (username == null && !quitFlag) {
            System.out.println("\n Scegli un'opzione: " +
                    "\n 1 Register" +
                    "\n 2 Login" +
                    "\n 3 Quit");
            op = scanner.nextInt();
            scanner.nextLine();
            switch (op) {
                case 1:
                    // Register
                    if (register(scanner))
                        // Se la registrazione va a buon fine
                        // allora il prossimo passo è fare la login
                        username = login(scanner);
                    break;
                case 2:
                    // Login
                    username = login(scanner);
                    break;
                case 3:
                    // Quit
                    closeChannel();
                    break;
                default:
                    System.out.println(" Errore nell'inserimento dell'opzione: " +
                            " digita 1 (per registrarti), 2 (per effettuare il login) o 3 (per uscire)");
                    break;
            }
        }
        if (!quitFlag)
            authenticatedLoop(scanner, username);
    }

    public void authenticatedLoop(Scanner scanner, String username) {
        int op;
        // entra in questo ciclo solo una volta che il log in è andato a buon fine
        // quindi posso offrire tutte le funzionalità di CROSS
        while (username != null && !quitFlag) {
            System.out.println("\n Scegli un'opzione: " +
                    "\n 1 Update Credentials" +
                    "\n 2 Insert Limit Order" +
                    "\n 3 Insert Market Order" +
                    "\n 4 Insert Stop Order" +
                    "\n 5 Cancel Order" +
                    "\n 6 Get Price History" +
                    "\n 7 Logout" +
                    "\n 8 Quit");
            op = scanner.nextInt();
            scanner.nextLine();

            switch (op) {
                case 1:
                    updateCredentials(scanner);
                    break;
                case 2:
                    insertLimitOrder(scanner);
                    break;
                case 3:
                    insertMarketOrder(scanner);
                    break;
                case 4:
                    insertStopOrder(scanner);
                    break;
                case 5:
                    cancelOrder(scanner);
                    break;
                case 6:
                    getPriceHistory(scanner);
                    break;
                case 7:
                    // Logout
                    if (logout(scanner, username)) {
                        // se L'op di logout ha successo
                        // allora l'username torna a essere null
                        // e si ritorna nella sezione delle op ridotte (per i non autenticati)
                        nonAuthenticatedLoop(scanner, null);
                    }
                    break;
                case 8:
                    // Quit
                    closeChannel();
                    break;
                default:
                    System.out.println(" Errore nell'inserimento dell'opzione: " +
                            " digita il numero corrispondente all'operazione che vuoi eseguire");
                    break;
            }
        }
    }

    /// Gestisce la scrittura sul canale
    private void handleWrite(String request) throws IOException {
        byte[] dataToWrite = request.getBytes(StandardCharsets.UTF_8);

        // Scrivi il messaggio sul WriteBuffer
        writeBuffer.clear();
        writeBuffer.put(dataToWrite);
        writeBuffer.flip();

        // Invia il messaggio sul canale
        while (writeBuffer.hasRemaining()) {
            socketChannel.write(writeBuffer);
        }
        System.out.println(" > Messaggio inviato: \n" + request);
        System.out.print("\n");
    }

    /// Gestisce la lettura dal canale
    private boolean handleRead() throws IOException {
        // Clears this buffer. The position is set to zero, the limit is set to the capacity, and the mark is discarded.
        readBuffer.clear();
        // Legge i dati dal canale e li memorizza nel buffer
        int bytesRead = socketChannel.read(readBuffer);

        // Se il client ha chiuso la connessione
        if (bytesRead == -1) {
            closeChannel();
            return false;
        }

        // Setta buffer per la lettura
        readBuffer.flip();
        byte[] data = new byte[readBuffer.remaining()];
        // Legge i dati dal buffer e li memorizza nell'array data
        readBuffer.get(data);

        if (isGetPriceHistory)
            return printArrayOnNewLines(data);
        else
            return printResponse(data);
    }

    ///  metodo utile per stampare su cli la risposta del server
    private boolean printResponse(byte[] jsonBytes) {
        // converte i dati da byte array a stringa
        String jsonResponse = new String(jsonBytes, StandardCharsets.UTF_8);
        // deserializza la stringa response
        Map<String, Object> response = gson.fromJson(jsonResponse, Map.class);
        // stampa il contenuto su cli
        for (Map.Entry<String, Object> entry : response.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        int orderErr = -1;
        if (response.containsKey("error") || (response.containsKey("orderId") && response.get("orderId").equals(orderErr)))
            return false;
        else
            return true;
    }

    /// Metodo utile per leggere e stampare su cli il risultato di getPriceHistory
    private boolean printArrayOnNewLines(byte[] jsonBytes) {
        // 1) Convertire byte[] in String (UTF-8)
        String json = new String(jsonBytes, StandardCharsets.UTF_8).trim();

        // Controllo semplice: per essere un array deve iniziare con '[' e finire con ']'
        if (!json.startsWith("[") || !json.endsWith("]"))
            return false;

        // 2) Analizza il JSON come array
        JsonArray array = JsonParser.parseString(json).getAsJsonArray();

        // 3) Stampa l’array con ogni elemento su una nuova riga
        System.out.println("[");
        for (int i = 0; i < array.size(); i++) {
            JsonElement elem = array.get(i);
            // elem.toString() produce il JSON compatto dell’elemento
            String obj = elem.toString();
            System.out.print("  " + obj);
            // Aggiungere la virgola tra gli oggetti (ma non dopo l’ultimo)
            if (i < array.size() - 1) {
                System.out.println(",");
            } else {
                System.out.println();
            }
        }
        System.out.println("]");
        return true;
    }

    /**
     * Gestisce l'invio della richiesta (serializzata in json) al server
     * e la recezione della risposta (serializzata in json) dal server
     */
    private boolean handleRequest(Request request) {
        try {
            // serializza in json l'oggetto request
            String jsonRequest = gson.toJson(request, Request.class);
            // invialo sul canale
            handleWrite(jsonRequest);
            // leggi la risposta
            return handleRead();
        } catch (IOException e) {
            System.err.println(" Errore nella gestione della connessione: " + e.getMessage());
            closeChannel();
            return false;
        }
    }

    /// Metodi di supporto per l'invio delle richieste

    private boolean register(Scanner scanner) {
        System.out.println(" Register ");

        // reperisci i valori per eseguire l'operazione
        System.out.print(" Username: \n > ");
        String user = scanner.nextLine().trim();
        System.out.print("\n");

        System.out.print(" Password: \n > ");
        String pass = scanner.nextLine().trim();
        System.out.print("\n");

        try {
            RegistrationResponseDTO response = registrationClient.register(user, pass);
            System.out.println(response.isSuccess() + " " + response.getMessage());
            return response.isSuccess() == 100;
        } catch (RemoteException e) {
            System.err.println(" 103 Errore durante la registrazione " + e.getMessage());
            return false;
        }
    }

    private String login(Scanner scanner) {
        System.out.println(" Login ");

        // reperisci i valori per eseguire l'operazione
        System.out.print(" Username: \n > ");
        String user = scanner.nextLine().trim();
        System.out.print("\n");

        System.out.print(" Password: \n > ");
        String pass = scanner.nextLine().trim();
        System.out.print("\n");

        System.out.println(" Threshold: \n > ");
        int threshold = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("\n");

        // componi l'oggetto request
        Request request = new Request.Builder("login")
                .addValue("username", user)
                .addValue("password", pass)
                .addValue("threshold", threshold)
                .build();

        // comunica l'op richiesta col server
        boolean success = handleRequest(request);

        // se il login ha avuto successo allora iscriviti al gruppo multicast
        if (success) {
            try {
                receiver = new MulticastNotificationReceiver(serverUdpPort,
                                                            multicastAddress,
                                                            udpReadBuffSize,
                                                            threshold);
                receiverThread = new Thread(receiver);
                receiverThread.start();
            } catch (IOException e) {
                System.err.println(" Errore durante la registrazione al gruppo multicast" + e.getMessage());
            }
        }

        // ritorna il nome utente se l'op è andata a buon fine
        return success ? user : null;
    }

    private void updateCredentials(Scanner scanner) {
        System.out.println(" Update Credentials ");

        // reperisci i valori per eseguire l'operazione
        System.out.print(" Username: \n > ");
        String user = scanner.nextLine().trim();
        System.out.print("\n");

        System.out.print(" Old password: \n > ");
        String oldPass = scanner.nextLine().trim();
        System.out.print("\n");

        System.out.print(" New password: \n > ");
        String newPass = scanner.nextLine().trim();
        System.out.print("\n");

        // componi l'oggetto request
        Request request = new Request.Builder("updateCredentials")
                .addValue("username", user)
                .addValue("old_password", oldPass)
                .addValue("new_password", newPass)
                .build();

        handleRequest(request);
    }

    private boolean logout (Scanner scanner, String username) {
        System.out.println(" Logout ");

        // componi l'oggetto request
        Request request = new Request.Builder("logout")
                .addValue("username", username)
                .build();

        return handleRequest(request);
    }

    private void insertLimitOrder(Scanner scanner) {
        System.out.println(" Insert Limit Order ");

        // reperisci i valori per eseguire l'operazione
        System.out.print(" Type: \n > ");
        String type = scanner.nextLine().trim();
        System.out.print("\n");

        System.out.print(" size: \n > ");
        int size = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("\n");

        System.out.print(" price: \n > ");
        int price = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("\n");

        // componi l'oggetto request
        Request request = new Request.Builder("insertLimitOrder")
                .addValue("type", type)
                .addValue("size", size)
                .addValue("price", price)
                .build();

        handleRequest(request);
    }

    private void insertMarketOrder(Scanner scanner) {
        System.out.println(" Insert Market Order ");

        // reperisci i valori per eseguire l'operazione
        System.out.print(" Type: \n > ");
        String type = scanner.nextLine().trim();
        System.out.print("\n");

        System.out.print(" size: \n > ");
        int size = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("\n");

        // componi l'oggetto request

        Request request = new Request.Builder("insertMarketOrder")
                .addValue("type", type)
                .addValue("size", size)
                .build();

        handleRequest(request);
    }

    private void insertStopOrder(Scanner scanner) {
        System.out.println(" Insert Stop Order ");

        // reperisci i valori per eseguire l'operazione
        System.out.print(" Type: \n > ");
        String type = scanner.nextLine().trim();
        System.out.print("\n");

        System.out.print(" size: \n > ");
        int size = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("\n");

        System.out.print(" price: \n > ");
        int price = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("\n");

        // componi l'oggetto request
        Request request = new Request.Builder("insertStopOrder")
                .addValue("type", type)
                .addValue("size", size)
                .addValue("price", price)
                .build();

        handleRequest(request);
    }

    private void cancelOrder(Scanner scanner) {
        System.out.println(" Cancel Order ");

        // reperisci i valori per eseguire l'operazione
        System.out.print(" OrderID: \n > ");
        int orderID = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("\n");

        // componi l'oggetto request
        Request request = new Request.Builder("cancelOrder")
                .addValue("orderId", orderID)
                .build();

        handleRequest(request);
    }

    private void getPriceHistory(Scanner scanner) {
        System.out.println(" Get Price History ");

        // reperisci i valori per eseguire l'operazione
        System.out.print(" Month: \n > ");
        int month = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("\n");

        System.out.print(" Year: \n > ");
        int year = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("\n");

        // componi l'oggetto request
        Request request = new Request.Builder("getPriceHistory")
                .addValue("month", month)
                .addValue("year", year)
                .build();

        isGetPriceHistory = true;

        handleRequest(request);
    }

    private void closeChannel() {
        try {
            // chiudi il canale se aperto
            if (socketChannel.isOpen())
                socketChannel.close();
            // chiudi tutte le altre risorse
            receiver.shutdown();
            receiverThread.join();
            // aggiorna la quit flag
            quitFlag = true;
            System.out.println(" Grazie per aver usato CROSS! A presto! ");
        } catch (IOException e) {
            System.err.println(" Errore durante la chiusura della connessione " + e.getMessage());
        } catch (InterruptedException e) {
            receiverThread.interrupt();
        }
    }
}
