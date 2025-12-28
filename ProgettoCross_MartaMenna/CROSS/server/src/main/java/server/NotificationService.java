package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;

/**
 * Classe che gestisce le notifiche multicast UDP per il superamento delle soglie di prezzo.
 * Utilizza uno scheduler interno per controllare periodicamente i prezzi e inviare notifiche
 * quando vengono superate le soglie registrate.
 */
public class NotificationService {
    // Porta UDP utilizzata per l'invio dei messaggi multicast
    private final int udpPort;
    // Socket multicast per l'invio delle notifiche
    private MulticastSocket multicastSocket;
    // Indirizzo del gruppo multicast
    private final InetAddress multicastGroup;
    // Riferimento al gestore dell'order book per ottenere i prezzi correnti
    private final OrderBookManager orderBookManager;
    // Set thread-safe ordinato delle soglie di prezzo registrate
    private final ConcurrentSkipListSet<Integer> thresholdSet;
    // Oggetto Gson per la serializzazione JSON
    private final Gson gson;
    // Scheduler per l'esecuzione periodica del controllo prezzi
    private final ScheduledExecutorService scheduler;

    /**
     * Costruttore del servizio di notifica.
     * Inizializza le risorse necessarie e avvia lo scheduler per il controllo dei prezzi.
     *
     * @param udpPort porta UDP per il multicast
     * @param multicastAddress indirizzo del gruppo multicast
     * @param orderBookManager gestore dell'order book per accedere ai prezzi
     * @throws IOException se si verificano errori nell'inizializzazione del socket
     */
    public NotificationService(int udpPort, String multicastAddress, OrderBookManager orderBookManager) 
            throws IOException {
        this.udpPort = udpPort;
        this.orderBookManager = orderBookManager;
        this.multicastGroup = InetAddress.getByName(multicastAddress);
        this.thresholdSet = new ConcurrentSkipListSet<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        
        initializeMulticastSocket();
        startPriceCheckScheduler();
    }

    /**
     * Inizializza il socket multicast con le configurazioni appropriate.
     */
    private void initializeMulticastSocket() throws IOException {
        this.multicastSocket = new MulticastSocket();
        // Limita i pacchetti alla rete locale
        this.multicastSocket.setTimeToLive(1);
    }

    /**
     * Avvia lo scheduler che controlla periodicamente i prezzi.
     * Il controllo viene eseguito ogni 30 secondi.
     */
    private void startPriceCheckScheduler() {
        scheduler.scheduleWithFixedDelay(
            this::checkAndNotify,  // metodo da eseguire
            30,                     // ritardo iniziale
            30,                     // periodo di esecuzione
            TimeUnit.SECONDS       // unità di tempo
        );
    }

    /**
     * Registra una nuova soglia di prezzo per le notifiche.
     * Le soglie non positive vengono silenziosamente ignorate.
     *
     * @param threshold la soglia di prezzo in millesimi di USD
     */
    public void registerMulticastPreference(int threshold) {
        if (threshold > 0)
            thresholdSet.add(threshold);
    }

    /**
     * Controlla se il prezzo corrente ha superato qualche soglia registrata
     * e invia le relative notifiche.
     */
    private void checkAndNotify() {
        try {
            // Ottiene il miglior prezzo di vendita corrente
            Map.Entry <Integer, PriceLevel> bestAsk = orderBookManager.getBestEntry("ASK");
            // se entrambe le strutture dati sono non vuote allora prosegui col check
            if (bestAsk != null && !thresholdSet.isEmpty()) {
                int currentPrice = bestAsk.getKey();
                // Controlla e notifica tutte le soglie superate
                while (!thresholdSet.isEmpty() && currentPrice >= thresholdSet.first()) {
                    sendMulticastNotification(currentPrice, thresholdSet.pollFirst());
                }
            }
        } catch (Exception e) {
            System.out.println("Errore durante il controllo dei prezzi: " + e.getMessage());
        }
    }

    /**
     * Invia una notifica multicast per una soglia superata.
     *
     * @param currentPrice il prezzo corrente che ha superato la soglia
     * @param threshold la soglia superata
     */
    private void sendMulticastNotification(int currentPrice, int threshold) {
        NotificationMessage message = new NotificationMessage("priceAlert", currentPrice, threshold);
        try {
            // Serializza direttamente con Gson
            byte[] data = gson.toJson(message).getBytes(StandardCharsets.UTF_8);
            
            // Crea e invia il pacchetto UDP
            DatagramPacket packet = new DatagramPacket(
                data, data.length, multicastGroup, udpPort
            );
            
            multicastSocket.send(packet);
            System.out.printf("Notifica inviata: prezzo %d ha superato soglia %d%n", 
                currentPrice, threshold);
            
        } catch (IOException e) {
            System.out.println("Errore nell'invio della notifica multicast: " + e.getMessage());
        }
    }

    /**
     * Arresta il servizio di notifica, chiudendo lo scheduler e il socket.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        
        if (multicastSocket != null && !multicastSocket.isClosed()) {
            multicastSocket.close();
        }
    }

    /**
     * Classe interna che rappresenta il formato del messaggio di notifica.
     */
    private static class NotificationMessage {
        private final String notification;
        private final int currentPrice;
        private final int threshold;

        public NotificationMessage(String notification, int currentPrice, int threshold) {
            this.notification = notification;
            this.currentPrice = currentPrice;
            this.threshold = threshold;
        }
    }
}