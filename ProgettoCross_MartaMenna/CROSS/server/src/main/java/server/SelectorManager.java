package server;

import common.ConfigLoader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class SelectorManager implements Runnable {
    
    private final Selector selector;
    private final ExecutorService threadPool;
    private final UserManager userManager;
    private final OrderBookManager orderBookManager;
    private final NotificationService notificationService;
    private final UDPNotificationSender udpNotificationSender;
    private final ConfigLoader config;
    
    /**
     * Classe che gestisce la 'selezione' degli eventi I/O asincroni
     * Il SelectorManager è gestito da un thread dedicato
     * 
     * @param selector il selettore per gli eventi I/O non bloccanti
     * @param threadPool al pool viene delegata l'esecuzione delle operazioni I/O
     * @param userManager gestore degli utenti per autenticazione e gestione sessioni
     * @param orderBookManager gestore del book degli ordini per salvataggio e matching
     * @param notificationService servizio per l'invio delle notifiche al gruppo multicast
     * @param udpNotificationSender servizio per l'invio di notifiche asincrone
     * @param config 'caricatore' del file di log del server
     *
     */
    public SelectorManager(Selector selector,
                           ExecutorService threadPool,
                           UserManager userManager,
                           OrderBookManager orderBookManager,
                           NotificationService notificationService,
                           UDPNotificationSender udpNotificationSender,
                           ConfigLoader config) {
        this.selector = selector;
        this.threadPool = threadPool;
        this.userManager = userManager;
        this.orderBookManager = orderBookManager;
        this.notificationService = notificationService;
        this.udpNotificationSender = udpNotificationSender;
        this.config = config;
    }
    
    @Override
    public void run() {
        System.out.println("SelectorManager avviato");
        
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Attendi gli eventi sui canali registrati
                selector.select();
                // Recupera le chiavi selezionate
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    if (key.isValid()) {
                        if (key.isAcceptable()) {
                            handleAccept(key);
                        } else if (key.isReadable() || key.isWritable()) {
                            handleReadWrite(key);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Errore nel ciclo di selectorManager" + e.getMessage());
            }
        }
    }
    
    /// Gestisce l'accettazione di una nuova connessione.
    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        // accetta la nuova connessione
        SocketChannel clientChannel = serverChannel.accept();
        // verifica che la connessione sia stata stabilita correttamente
        if (clientChannel != null) {
            clientChannel.configureBlocking(false);
            // ip e porta ricavati
            InetSocketAddress remoteAddress = (InetSocketAddress) clientChannel.getRemoteAddress();
            // Registra il canale con il selettore per gli eventi di lettura
            SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_READ);
            // istanzia un nuovo ConnectionHandler per gestire la connessione
            ConnectionHandler handler = new ConnectionHandler(clientKey, remoteAddress, userManager, orderBookManager, udpNotificationSender, notificationService, config);
            // Associa il ConnectionHandler alla chiave di selezione
            clientKey.attach(handler);
            System.out.println("Nuova connessione accettata: " + clientChannel.getRemoteAddress());
        }
    }
    
    /// Gestisce le chiavi con eventi di lettura o scrittura (delegando il task al thread pool)
    private void handleReadWrite(SelectionKey key) {
        // Recupera il ConnectionHandler associato alla chiave
        ConnectionHandler handler = (ConnectionHandler) key.attachment();

        if (handler != null) {
            // passa il ConnectionHandler al thread pool per la gestione del task
            threadPool.execute(handler);
        } else {
            System.out.println("Nessun ConnectionHandler associato alla chiave!");
            removeKey(key);
        }
    }
    
    private void removeKey(SelectionKey key) {
        Channel channel = key.channel();
        // rimuove il canale a cui è associato key.cancel() dal Selector
        key.cancel();
        try {
            channel.close();
        } catch (IOException ex) {
            System.out.println("Errore nella chiusura del canale" + ex.getMessage());
        }
    }
}