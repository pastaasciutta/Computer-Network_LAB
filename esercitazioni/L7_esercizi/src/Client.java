/* Esercizio UDPPingPong
  L'esercizio consiste nella scrittura di un server che offre il servizio di "Ping Pong"
  e del relativo programma client.

 Un client invia al server un messaggio di "Ping".

 Il server, se riceve il messaggio, risponde con un messaggio di "Pong".

 Il client sta in attesa n secondi di ricevere il messaggio dal server (timeout) e poi termina.

 Client e Server usano il protocollo UDP per lo scambio di messaggi.*/

import java.net.*;

public class Client {
    // Dimensione del buffer per i pacchetti di risposta.
    public static final int bufSize = 1024;
    // Numero di porta del server a cui connettersi.
    public static final int port = 9999;
    // Tempo massimo di attesa per la risposta del server.
    public static final int timeout = 60000;
    public static void main(String[] args) {
        DatagramSocket clientSocket = null;
        // Il contenuto dei messaggi inviati dal client
        // e' semplicemente la stringa "PING".
        byte[] content = new String("PING").getBytes();
        byte[] buf = new byte[bufSize];
        try {
            // Inizializzo la socket per inviare le richieste.
            clientSocket = new DatagramSocket();
            // Imposto un timeout in modo tale da far terminare
            // il client nel caso in cui non riceva risposta dal server.
            clientSocket.setSoTimeout(timeout);
            // Preparo il pacchetto da inviare e lo invio.
            // L'indirizzo del server è quello associato a "localhost"
            // ovvero il classico 127.0.0.1.
            InetAddress addr = InetAddress.getByName("localhost");
            DatagramPacket sp = new DatagramPacket(
                    content, content.length, addr, port);
            clientSocket.send(sp);
            System.out.println("Richiesta inviata");
            // Attendo una risposta dal server.
            DatagramPacket rp = new DatagramPacket(buf, buf.length);
            clientSocket.receive(rp);
            System.out.printf("Risposta ricevuta: %s\n",
                    new String(rp.getData()));
        }
        catch (SocketTimeoutException e) {
            // Qui catturo l'eccezione che viene sollevata
            // allo scadere del tempo massimo di attesa
            // sulla socket.
            System.out.println("Nessuna risposta ricevuta.");
        }
        catch (Exception e) {
            System.err.println("Errore: " + e.getMessage());
        }
        finally {
            clientSocket.close();
            System.out.println("Client terminato.");
        }
    }
}