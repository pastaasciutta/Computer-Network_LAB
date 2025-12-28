package server;

import server.OrderTypes.Order;

import java.net.InetSocketAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// Aggiunto per JSON
import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;

/**
 * Classe utile per l'invio di notifiche UDP asincrone ai client registrati.
 * Mappa ogni orderId (Integer) a una InetSocketAddress e invia un datagramma UDP
 * quando l'ordine è erogato.
 */
public class UDPNotificationSender {

    // Mappa concorrente da orderId a indirizzo IP/porta
    private final ConcurrentMap<Integer, InetSocketAddress> orderMap;
    private final Gson gson;

    /**
     * Costruttore: inizializza la mappa interna.
     */
    public UDPNotificationSender() {
        this.orderMap = new ConcurrentHashMap<>();
        this.gson = new Gson();
    }

    /**
     * Aggiunge un nuovo ordine con il relativo indirizzo IP e porta alla mappa.
     * @param orderId identificatore dell'ordine
     * @param address socket address del client
     */
    public void add(int orderId, InetSocketAddress address) {
        orderMap.put(orderId, address);
    }

    public void remove (int orderId) {
        orderMap.remove(orderId);
    }

    /**
     * Rimuove gli orderId forniti e invia una notifica UDP per ciascun ordine.
     * @param orders lista di ordini erogati di cui inviare la notifica
     */
    public void removeAndNotify(List<server.OrderTypes.Order> orders) {
        if (orders == null) {
            return;
        }

        for (Order order : orders) {
            int orderId = order.getOrderId();
            // Recupera l'indirizzo associato a questo orderId
            InetSocketAddress address = orderMap.get(orderId);

            if (address != null) {
                // Invia la notifica UDP
                notify(order, address);
                // Rimuove l'entry dalla mappa dopo l'invio
                orderMap.remove(orderId);
            }
            // Se address è null, l'ordine non era nella mappa; lo ignoriamo
        }
    }

    /**
     * Metodo interno che costruisce un JSON di notifica e invia un pacchetto UDP.
     * Il JSON contiene i campi:
     * {
     *   "status": "erogato",
     *   "order": { ... campi dell'ordine ... }
     * }
     *
     * @param order ordine da notificare
     * @param address socket address di destinazione
     */
    private void notify(server.OrderTypes.Order order, InetSocketAddress address) {
        // Costruzione dell'oggetto di notifica
        Notification notification = new Notification("erogato", order);

        // Serializzazione in JSON
        String json = gson.toJson(notification);
        byte[] data = json.getBytes(StandardCharsets.UTF_8);

        // Invio del pacchetto UDP
        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket packet = new DatagramPacket(
                    data,
                    data.length,
                    address
            );
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * DTO per la notifica JSON: contiene lo status e l'ordine stesso.
     */
    private static class Notification {
        private final String status;
        private final Order order;

        public Notification(String status, Order order) {
            this.status = status;
            this.order = order;
        }
    }
}