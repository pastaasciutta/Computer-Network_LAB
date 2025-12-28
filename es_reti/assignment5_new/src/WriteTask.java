import java.io.*;

public class WriteTask implements Runnable {

    private AlphabetCHM map;
    public WriteTask(AlphabetCHM map){ this.map = map; }

    @Override
    public void run() {

        try {
            DataOutputStream outputStream =
                    new DataOutputStream(new BufferedOutputStream(new FileOutputStream("./occorrenze")));

            for(char letter = 'a'; letter <= 'z'; letter++){
                int value = map.getValue(letter);
                String s = "\t" + letter + "," + value + "\n";
                //outputStream.writeBytes(s);
                outputStream.write(s.getBytes());
                System.out.println(s);
            }

            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
