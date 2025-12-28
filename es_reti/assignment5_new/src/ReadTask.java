import java.io.*;

public class ReadTask implements Runnable {

    private String input;
    private AlphabetCHM map;
    public ReadTask(String input, AlphabetCHM map) {
        this.input = input;
        this.map = map;
    }

    @Override
    public void run() {

        LocalAlphabetHM localMap = new LocalAlphabetHM();
        File f = new File(input);
        int len = (int) f.length();
        this.FileIS(input, localMap, len);
    }

    //creating a channel to read from file
    public void FileIS(String filePath, LocalAlphabetHM localMap, int n) {
          try {
              //opening resources
              DataInputStream inputStream =
                      new DataInputStream(new BufferedInputStream( new FileInputStream(filePath)));

              byte[] buffer = new byte[n];

              while( inputStream.read(buffer, 0, n-1) != -1) {
                  for (byte b : buffer)
                      localMap.Add(Character.toLowerCase((char)b));
              }
              //closing resources
              inputStream.close();

              for(char letter = 'a'; letter <= 'z'; letter++)
                  map.Add(letter, localMap.getValue((char)letter));

          } catch (FileNotFoundException e){
              e.printStackTrace();
          } catch (IOException e) {
              throw new RuntimeException(e);
          }
      }
}

/**
 * public void FileIS(String filePath, LocalAlphabetHM localMap, int n) {
 *         try {
 *             //opening resources
 *             DataInputStream inputStream =
 *                     new DataInputStream(new BufferedInputStream( new FileInputStream(filePath)));
 *
 *             byte[] buffer = new byte[n];
 *
 *             while( inputStream.read(buffer, off, n-1) != -1) {{}
 *
 *                 for (byte b : buffer)
 *                     localMap.Add((char) b);
 *             }
 *             //closing resources
 *             inputStream.close();
 *
 *         } catch (FileNotFoundException e){
 *             e.printStackTrace();
 *         } catch (IOException e) {
 *             throw new RuntimeException(e);
 *         } finally {
 *             for(char letter = 'a'; letter <= 'z'; letter++)
 *                 map.Add(letter, localMap.getValue(letter));
 *         }
 *     }
 */


/**
 * public void FileIS(String filePath, LocalAlphabetHM localMap) {
 *         try {
 *             //opening resources
 *             Scanner inputStream =
 *                     new Scanner(new BufferedInputStream( new FileInputStream(filePath)));
 *
 *             //byte[] buffer = new byte[n]; int off=0;
 *             String line = "";
 *
 *             while( inputStream.hasNext()) {
 *                 line = inputStream.nextLine();
 *
 *                 line.toLowerCase();
 *
 *                 for (int i=0; i < line.length(); i++)
 *                     localMap.Add(line.charAt(i));
 *                 //off = inputStream.read(buffer, off, n);
 *             }
 *             //closing resources
 *             inputStream.close();
 *
 *         } catch (FileNotFoundException e){
 *             e.printStackTrace();
 *         }
 *     }
 */
