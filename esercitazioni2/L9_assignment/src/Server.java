/*Java NIO echo*/
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class Server {
    public static final int DEFAULT_PORT = 1919;

    public static void main(String[] args) {

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (RuntimeException e) {
            port = DEFAULT_PORT;
        }

        System.out.println("server listening for connections on port " + port);

        //invoco open che instaura conneione
        try{
            open(port);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void open (int port) throws IOException {

        ServerSocketChannel serverChannel;
        Selector selector;

        try {
            // apro ServerSocketChannel
            serverChannel = ServerSocketChannel.open();
            // apro una ServerSocket su ServerSocketChannel
            ServerSocket ss = serverChannel.socket();
            //hostname non specificato, quindi è sottinteso il localhost
            InetSocketAddress address = new InetSocketAddress(port);
            // lego il canale ad address
            ss.bind(address);
            // ServerChannel configurato come non bloccante
            serverChannel.configureBlocking(false);
            //attivo il selettore
            selector = Selector.open();
            //registro ServerChannel in selector
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        while (true) {

            try {
                /* select() method: selects a set of keys
                   whose corresponding channels are ready for I/O operations.
                   This method performs a blocking selection operation.*/
                selector.select();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            Set<SelectionKey> readyKeys = selector.selectedKeys();
            SelectionKey key = null;
            Iterator<SelectionKey> iterator = readyKeys.iterator();

            while (iterator.hasNext()) {
                //estraggo la chiave
                key = iterator.next();
                // rimuovo la chiave da Selected set affiche non sia processsata nuovamente (ma non da registered Set)
                iterator.remove();

                try {

                    if (key.isAcceptable()) {
                        accept(key);
                    }

                    else if (key.isReadable()) {
                        String message = read(key);
                        if (message.length() > 0){

                            /* equalsIgnoreCase Compares this String to another String, ignoring case considerations.
                           Two strings are considered equal ignoring case
                           if they are of the same length and corresponding Unicode code
                           points in the two strings are equal ignoring case.*/
                            if (message.equalsIgnoreCase("exit")) {
                                // cancello la chiave se il cliente digita exit
                                key.cancel();
                            } else {
                                echo(key, message);
                            }
                        }
                    }
                } catch (IOException ex) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        break;
                    }
                    System.out.println(ex.getMessage());
                    break;
                }
            }
        }
    }

    public static void accept(SelectionKey key) throws IOException {
        //stabilisce la connessione sul canale segnalato da key
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        //simula cliente e accetta la connessione
        SocketChannel client = server.accept();
        //setta cliente come non bloccante
        client.configureBlocking(false);

        System.out.println("server accepted connection from " + client);
        //registro canale in selector
        client.register(key.selector(), SelectionKey.OP_READ);
    }

    public static String read(SelectionKey key) throws IOException {
        //stabilisce la connessione sul canale segnalato da key
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer input = ByteBuffer.allocate(1024);

        int bytesCount = client.read(input);
        String buffer = "";

        if (bytesCount > 0) {
            input.flip();
            Charset charset = StandardCharsets.UTF_8;
            // costruisco un nuovo decoder per questo charset
            CharsetDecoder decoder = charset.newDecoder();
            /* decode() method that decodes the remaining content of a single input byte buffer
               into a newly-allocated character buffer*/
            CharBuffer charBuffer = decoder.decode(input);
            //converto charBuffer in stringha per copiare il contenuto in buffer
            buffer = charBuffer.toString();
        }

        if (buffer.equalsIgnoreCase("exit"))
            return "exit";

        return "echo "+buffer;
    }

    public static void echo(SelectionKey key, String m) throws IOException {
        //stabilisce la connessione sul canale segnalato da key
        SocketChannel client = (SocketChannel) key.channel();
        //wrap(): wraps a byte array into a buffer
        ByteBuffer buffer = ByteBuffer.wrap(m.getBytes());
        //invio del messaggio
        client.write(buffer);
        System.out.println("server" + m + " succeed");
    }
}
