import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class Client {

    private static BufferedReader input;
    public static final int DEFAULT_PORT = 1919;

    public static void main(String[] args) {

        int port;
        if (args.length > 0) {

            //inizializzo porta
            try{
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException n){
                port = DEFAULT_PORT;
            }

            InetSocketAddress address = null;
            Selector selector;

            try{
                InetAddress host = InetAddress.getByName("localhost");
                address = new InetSocketAddress(host, port);
                System.out.println("client connecting to " + address.getHostName());
            } catch (UnknownHostException e) {
                System.out.println(e.getMessage());
            }

            //avvio selettore e cerco di stabilire una connessione
            try{
                selector = Selector.open();
                connecting(address, selector);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void connecting(InetSocketAddress address, Selector selector) {

        try(SocketChannel socketChannel = SocketChannel.open()) {
            //config non bloccante
            socketChannel.configureBlocking(false);

            socketChannel.connect(address);

            // registro socket chan sul selettore
            socketChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);

            // acquisisco l'input
            input = new BufferedReader(new InputStreamReader(System.in));

            boolean done = false;

            while (!done) {
                if(selector.select() > 0)
                    done = processSelect(selector.selectedKeys());
            }

        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public static boolean processSelect(Set<SelectionKey> selectedKeys) throws IOException {

        SelectionKey key = null;
        Iterator<SelectionKey> iterator = null;
        //iteratore utile a muovermi nel set selected keys
        iterator = selectedKeys.iterator();

        while (iterator.hasNext()) {
            //estraggo chiave
            key = iterator.next();
            iterator.remove();

            //caso connessione
            if (key.isConnectable()) {
                boolean connected = processConnect(key);
                if (!connected) {
                    break;
                }
            }

            //caso lettura
            if (key.isReadable()) {
                String msg = "";
                try{
                    msg = processRead(key);
                } catch (CharacterCodingException e) {
                    System.out.println(e.getMessage());
                }
            }

            //caso scrittura
            if (key.isWritable()) {
                try{
                    System.out.print("client Enter your text\n");
                    //prendo il messagio dell'user
                    String msg = input.readLine();

                    if (msg.equalsIgnoreCase("exit")) {
                        processWrite(key, msg);
                        break;
                    }
                    processWrite(key, msg);

                }catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }

        }
        //done ancora uguale a falso
        return false;
    }

    private static void processWrite(SelectionKey key, String msg) {

        SocketChannel sChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
        try{
            //scrive sul canale
            sChannel.write(buffer);
            //cambia stato chiave
            sChannel.register(key.selector(), SelectionKey.OP_READ);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static String processRead(SelectionKey key) throws CharacterCodingException {

        SocketChannel sChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        try{
            sChannel.read(buffer);
            //preparo per lettura
            buffer.flip();

            Charset charset = StandardCharsets.UTF_8;
            CharsetDecoder decoder = charset.newDecoder();
            CharBuffer charBuffer = decoder.decode(buffer);

            sChannel.register(key.selector(), SelectionKey.OP_WRITE);
            return charBuffer.toString();

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

    private static boolean processConnect(SelectionKey key) {

        SocketChannel channel = (SocketChannel) key.channel();
        try {
            /* isConnectionPending() Tells whether or not
               a connection operation is in progress on this channel.*/
            while (channel.isConnectionPending()) {
                channel.finishConnect();
            }
        } catch (IOException e) {
            // Cancello canale dalle selkeys
            key.cancel();
            System.out.println("client error connecting " + e.getMessage());
            return false;
        }
        return true;
    }
}