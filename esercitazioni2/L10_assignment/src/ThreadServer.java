import java.io.IOException;
import java.net.*;

public class ThreadServer extends Thread{

    private static final int SERVER_PORT = 8888;

    private InetAddress groupAddress;
    private DatagramSocket socket = null;
    private final int port;

    public ThreadServer(String groupAddress, int port) {

        this.port = port;

        try{
            this.socket = new DatagramSocket(SERVER_PORT);
            this.groupAddress = InetAddress.getByName(groupAddress);

        }catch (SocketException | UnknownHostException e){
            System.out.println(e.getMessage());
            //errore uscita forzata
            System.exit(1);
        }

        if(!this.groupAddress.isMulticastAddress()){
            throw new IllegalArgumentException(groupAddress + " is not a multicast address.");
        }
    }

    //interrompo thread
    public void interrupt(){
        Thread.currentThread().interrupt();
        if(socket != null){
            //chiudo anche la socket nel caso in cui fosse aperta
            socket.close();
        }
    }

    //invio il tempo corrente
    public void run(){

        try{
            while(!Thread.currentThread().isInterrupted()){
                // ottengo il tempo
                String time = String.valueOf(System.currentTimeMillis());
                // elaboro packet con time all'interno
                DatagramPacket packet = new DatagramPacket( time.getBytes(), time.length(), groupAddress, port);
                // invio
                socket.send(packet);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}