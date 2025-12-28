import java.io.*;
import java.util.zip.*;

public class Compression implements Runnable{

    private DirectoryList list;

    public Compression (DirectoryList list) { this.list = list; }

    @Override
    public void run() {
        if (list.isEmpty())
            return;
        //preleva testa della list di directories
        String currentDirectory = list.getHead();
        //crea stringa di filepath dei file presenti in current directory
        String[] filesInCurrentDirectory = (new File(currentDirectory)).list();

        //SE la directory è non vuota
        if ( filesInCurrentDirectory != null ){
            //ALLORA visitala
            for (String filePath : filesInCurrentDirectory){
                String absoluteFilePath = currentDirectory.concat("/"+filePath);
                File current = new File(absoluteFilePath);
                if( current.isFile() && !(current.isHidden() || filePath.contains(".gz")) ){
                    System.out.println("\t...compressing file: " + filePath);
                    GzipCompression(absoluteFilePath);
                }
            }
        }
    }

    private void GzipCompression (String s){
        try {
            //opening resources
            BufferedInputStream in = new BufferedInputStream( new FileInputStream(s));
            BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream(s.concat(".gz")));
            GZIPOutputStream gzipOut = new GZIPOutputStream(out);

            //gzipOS.write(fileIS.read());

            //creo buffer su cui scrivere cio che leggo da in e leggere cio che scrivo in out
            byte[] buffer = new byte[1024];
            int len;
            while( (len= in.read(buffer)) != -1)
                gzipOut.write(buffer, 0, len);

            //close resources
            gzipOut.close();
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
