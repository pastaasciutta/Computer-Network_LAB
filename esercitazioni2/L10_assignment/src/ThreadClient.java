import java.io.IOException;
import java.net.*;
import java.util.Calendar;

public class ThreadClient extends Thread{

    private final int port;
    private MulticastSocket socket;
    private final InetAddress group;

    public ThreadClient(String group, int port) throws UnknownHostException, IllegalArgumentException {
        this.port = port;
        this.group = InetAddress.getByName(group);

        // controllo se group è un indirizzo multicast
        if(!this.group.isMulticastAddress()) {
            throw new IllegalArgumentException(group + " is not a multicast address");
        }
    }

    //per abbandonare multicast group
    public void interrupt() {
        try {
            InetSocketAddress group = new InetSocketAddress(this.group, this.port);
            //abbandono
            socket.leaveGroup(group, NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        //chiudo listener
        socket.close();

        try {
            //aspetto 3000 millisec prima che il thread muoia
            this.join(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\nclient interrupted");
    }

    public void run(){

        socket = null;
        try {
            socket = new MulticastSocket(port);
            // Setto time to leave a 1
            socket.setTimeToLive(1);
            // acquisisco l'indirizzo socket per entrare nel multicast group
            InetSocketAddress group = new InetSocketAddress(this.group, this.port);
            // interfaccia network locale
            NetworkInterface netInt = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            // entro nel multicast group
            socket.joinGroup(group, netInt);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("client started");

        int len = 64;
        //buffer per allocare messaggio
        byte[] buffer = new byte[len];
        System.out.println("listening on port " + port);

        for (int i = 0; i < 10; i++) {

            DatagramPacket dat = new DatagramPacket(buffer, buffer.length);
            assert socket != null;

            try {
                socket.receive(dat);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return;
            }

            String timestamp = new String(dat.getData(), dat.getOffset(), dat.getLength());
            printd(timestamp);
        }
        this.interrupt();
    }

    private void printd(String timestamp) {

        Calendar c = Calendar.getInstance();
        try{
            long millis = Long.parseLong(timestamp);
            c.setTimeInMillis(millis);

            System.out.printf("\r%d/%d/%d %d:%d:%d\n", c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH),
                    c.get(Calendar.YEAR), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
        }
        catch (NumberFormatException e){
            System.out.println(e.getMessage());
        }
    }
}
