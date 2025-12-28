package client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

/**
 * Classe utile per ricevere le notifiche multicast.
 * Il client si unisce a un gruppo multicast e ascolta le notifiche di prezzo.
 */
public class MulticastNotificationReceiver implements Runnable {
    private final int BUFFER_SIZE;

    private final MulticastSocket multicastSocket;
    private final InetAddress multicastGroup;
    private final Gson gson;
    private final int userThreshold;
    private volatile boolean running;

    /**
     * Costruisce un nuovo ricevitore di notifiche.
     *
     * @param udpPort porta UDP per il multicast
     * @param multicastAddress indirizzo del gruppo multicast
     * @param userThreshold soglia di prezzo specificata dall'utente
     * @throws IOException se si verificano errori nell'inizializzazione del socket
     */
    public MulticastNotificationReceiver(int udpPort, String multicastAddress, int bufferSize, int userThreshold)
            throws IOException {
        this.BUFFER_SIZE = bufferSize;
        this.multicastSocket = new MulticastSocket(udpPort);
        this.multicastGroup = InetAddress.getByName(multicastAddress);
        this.multicastSocket.joinGroup(multicastGroup);
        this.userThreshold = userThreshold;
        this.gson = new GsonBuilder().create();
        this.running = true;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];

        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);

                String jsonMessage = new String(
                        packet.getData(),
                        0,
                        packet.getLength(),
                        StandardCharsets.UTF_8
                );

                NotificationMessage message = gson.fromJson(jsonMessage, NotificationMessage.class);

                // Mostra la notifica solo se corrisponde alla soglia dell'utente
                if (message.threshold == userThreshold) {
                    System.out.printf(" Notifica: Il prezzo corrente (%d) ha superato la tua soglia (%d)%n",
                            message.currentPrice, message.threshold);
                }

            } catch (IOException e) {
                if (running) {
                    System.out.println(" Errore nella ricezione della notifica: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Arresta il ricevitore di notifiche.
     */
    public void shutdown() {
        running = false;
        try {
            multicastSocket.leaveGroup(multicastGroup);
        } catch (IOException e) {
            System.out.println(" Errore nell'uscita dal gruppo multicast: " + e.getMessage());
        }
        multicastSocket.close();
    }

    /**
     * Classe che rappresenta il formato del messaggio di notifica.
     */
    private static class NotificationMessage {
        private String notification;
        private int currentPrice;
        private int threshold;
    }
}