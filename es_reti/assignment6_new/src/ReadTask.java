import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class ReadTask implements Runnable {

    private String input;
    private int nThread;
    private int len;
    private String[] someLines;
    /** @param input is the filepath taken by command line
     *  @param someLines is a group of lines from the file denotated by the previous fileath
     *  @param nThread id dell'nesimo thread a lavoro (0 per il single thread e il lettore del pool,
     *                 >1 per tutti i thread del pool)
     *  @param len lenght of the file denotated by filepath input
     */
    public ReadTask (String input, String[] someLines, int nThread, int len) {
        this.input = input;
        this.someLines = someLines;
        this.nThread = nThread;
        this.len = len;
    }

    @Override
    public void run() {
        this.formattedFile();
    }

    //creating a channel to read from file
    public void formattedFile() {
        //se sei il single tread o il lettore del pool
        if (nThread==0) {
            try {
                //allora leggi il file e dopo formattalo
                IPtoHostName(FileIS());
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        else
            //altrimenti formatta linee gia lette da inputstream
            IPtoHostName(someLines);
    }

    /** @param lines linee del file (array di strings separate da \n) che contengono indirizzi IP */
    public void IPtoHostName(String[] lines){

        for(String line : lines) {
            int i = line.indexOf(" ");
            String ip = line.substring(0, i);
            String formattedLine = "";
            try {
                InetAddress address = InetAddress.getByName(ip);
                String hostName = address.getHostName();
                formattedLine = line.replaceAll(ip, hostName);
            } catch (UnknownHostException e){
                formattedLine = line.replaceFirst(ip, "UnknownHostException");
            }
            System.out.println(formattedLine);
        }
    }

    public String[] FileIS() throws IOException {

        //opening resources
        DataInputStream inputStream =
                new DataInputStream(new BufferedInputStream(new FileInputStream(input)));

        byte[] buffer = new byte[len];

        int currentLen = inputStream.readNBytes(buffer, 0, len);
        //closing resources
        inputStream.close();

        String text = new String(buffer, StandardCharsets.UTF_8);

        return text.split("\n");
    }
}
