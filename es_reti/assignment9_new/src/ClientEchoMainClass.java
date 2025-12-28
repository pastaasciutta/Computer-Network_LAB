import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/* Il client legge il messaggio da inviare da console,
 * lo invia al server e visualizza quanto ricevuto dal server.*/
public class ClientEchoMainClass {

    static final int port = 8888;
    static final int capacity = 1024;
    public static void main(String[] args) {
        System.out.println("\twaiting for text..");

        Scanner scanner = new Scanner(System.in);
        String text = scanner.nextLine();
        scanner.close();

        try {
            SocketAddress address = new InetSocketAddress (InetAddress.getLocalHost(), port);
            SocketChannel client = SocketChannel.open(address);

            ByteBuffer buffer = ByteBuffer.allocate(capacity);
            buffer = ByteBuffer.wrap(text.getBytes());

            if (client.finishConnect())
                client.write(buffer);

            while (client.read (buffer) != -1) {
                //setto buffer per la lettura
                buffer.flip();

                while (buffer.hasRemaining())
                    System.out.println(buffer);
                //tutti i dati sono stati letti e scaricati sul file

                // setto buffer per la scrittura
                buffer.clear();
            }
            client.close();

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
