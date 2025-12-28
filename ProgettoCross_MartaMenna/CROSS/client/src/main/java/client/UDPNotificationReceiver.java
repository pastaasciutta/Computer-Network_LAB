package client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Gson per il JSON pretty-print
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * UDPNotificationReceiver ascolta su una porta UDP specificata,
 * riceve messaggi e li stampa su cli.
 */
public class UDPNotificationReceiver implements Runnable {
    private final int port;
    private DatagramSocket socket;
    byte[] buffer;
    private volatile boolean running;
    private final Gson gson;

    /**
     * Costruisce il receiver legandolo alla porta locale.
     * @param port Porta UDP su cui ascoltare
     * @throws SocketException se non è possibile bindare il socket
     */
    public UDPNotificationReceiver(int port, int bufferLen) throws SocketException {
        this.port = port;
        this.socket = new DatagramSocket(this.port);
        this.buffer = new byte[bufferLen];
        this.running = true;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Loop principale: riceve pacchetti, decodifica in UTF-8 e stampa.
     */
    @Override
    public void run() {
        System.out.println("[UDP Receiver] Avviato sulla porta " + port + ".");

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (running) {
            try {
                socket.receive(packet);
                String rawMsg = new String(
                        packet.getData(),
                        0,
                        packet.getLength(),
                        StandardCharsets.UTF_8
                );

                // Prova a fare pretty-printing se il messaggio è in JSON
                try {
                    Object json = gson.fromJson(rawMsg, Object.class);
                    String prettyJson = gson.toJson(json);
                    System.out.println("[Nuova notifica ricevuta]");
                    System.out.println(prettyJson);
                } catch (JsonSyntaxException ex) {
                    // Non è JSON: stampa grezzo
                    System.out.println("[Nuova notifica ricevuta]");
                    System.out.println(rawMsg);
                }

            } catch (IOException e) {
                if (running) {
                    System.err.println("Errore in receive(): " + e.getMessage());
                }
                break; // esce se il socket è chiuso
            }
        }

        System.out.println("[UDP Receiver] Terminato.");
    }

    /**
     * Ferma il loop di ricezione e chiude il socket.
     */
    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}