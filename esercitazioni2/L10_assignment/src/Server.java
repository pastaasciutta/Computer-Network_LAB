import java.io.IOException;

public class Server {

    private static int port;
    private static String address;

    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("Usage: java Server <Address> <target port>");
            System.exit(1);
        }

        // acquisco indirizzo e porta
        try{
            address = args[0];
            port = Integer.parseInt(args[1]);
        }catch (NumberFormatException e){
            System.out.println(e.getMessage());
            System.exit(1);
        }

        try{
            //creo threadserver
            ThreadServer MTS;
            MTS = new ThreadServer(address, port);
            /*per passare la gestione di MTS alla JVM reo un oggetto thread associato
              cosi da non dover gestire chiusura o eccezioni fatali
              (affinche thread venga gestito, si stppa il tread main
              (non è il caso di farlo con piu thread))*/
            Thread thread = new Thread(MTS);
            //attivo thread
            thread.start();

            System.out.println("server on "+ address + " " + port);
            System.out.println("Press ENTER to stop the server");

            //prendo l'input dell'user
            System.in.read();
            //termino server worker
            MTS.interrupt();

            thread.join(3000);

        }catch (IOException | InterruptedException | IllegalArgumentException e){

            System.out.println(e.getMessage());
            System.exit(1);
        }

        System.out.println("server closed");
        System.exit(0);
    }
}
