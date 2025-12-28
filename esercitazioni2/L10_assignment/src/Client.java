import java.net.UnknownHostException;

public class Client {

    private static int port;
    private static String address;

    public static void main(String[] args) {

        ThreadClient listener;
        if(args.length != 2) {
            System.out.println("Usage: java Client <address> <listening port>");
            //errore
            System.exit(1);
        }

        //setto client
        try{
            //acquisisco indirizzo e porta
            address = args[0];
            port = Integer.parseInt(args[1]);

        }catch (NumberFormatException e){
            System.out.println("wrong port insertion");
            System.exit(1);
        }

        try{
            listener = new ThreadClient(address, port);
            //attivo thread listener
            listener.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}