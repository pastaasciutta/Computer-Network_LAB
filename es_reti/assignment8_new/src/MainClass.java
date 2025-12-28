import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/*
* Scopo dell'assignment è dare una valutazione delle prestazioni
* di diverse strategie di bufferizzazione di I/O offerte da JAVA
*
* scrivere un programma che copi un file di input in un file di output,
* utilizzandole seguenti modalità alternative di bufferizzazione,
* valutando il tempo impiegato per la copia del file in ognuna delle seguenti strategie:
*   FileChannel con buffer indiretti
*   FileChannel con buffer diretti
*   FileChannel utilizzando l'operazione transferTo()
*   Buffered Stream di I/O
*   stream letto in un byte-array gestito dal programmatore
*
* confrontare le prestazioni delle diverse soluzioni,
* variando la dimensione del file (da qualche kbyte fino ad almeno una decina di Megabyte)
* e la dimensione del buffer.
*
* riportare i risultati ottenuti nel sorgente, in un commento:
* Test 10Kb
*   FileChannel con buffer indiretti:                        11 milliSec
*   FileChannel con buffer diretti:                          9 milliSec
*   FileChannel utilizzando l'operazione transferTo(): 1     6 milliSec
*   Buffered Stream di I/O:                                  0 milliSec
*   stream letto in un byte-array gestito dal programmatore: 0 milliSec
* Test 100Kb
*   FileChannel con buffer indiretti:                        35 milliSec
*   FileChannel con buffer diretti:                          6 milliSec
*   FileChannel utilizzando l'operazione transferTo():       30 milliSec
*   Buffered Stream di I/O:                                  10 milliSec
*   stream letto in un byte-array gestito dal programmatore: 1 milliSec
* Test 1Mb
*   FileChannel con buffer indiretti:                        58 milliSec
*   FileChannel con buffer diretti:                          20 milliSec
*   FileChannel utilizzando l'operazione transferTo():       5 milliSec
*   Buffered Stream di I/O:                                  5 milliSec
*   stream letto in un byte-array gestito dal programmatore: 16 milliSec
* Test 10Mb
*   FileChannel con buffer indiretti:                        418 milliSec
*   FileChannel con buffer diretti:                          216 milliSec
*   FileChannel utilizzando l'operazione transferTo():       17 milliSec
*   Buffered Stream di I/O:                                  44 milliSec
*   stream letto in un byte-array gestito dal programmatore: 134 milliSec
* Test 100Mb
*   FileChannel con buffer indiretti:                        2191 milliSec
*   FileChannel con buffer diretti:                          1247 milliSec
*   FileChannel utilizzando l'operazione transferTo():       145 milliSec
*   Buffered Stream di I/O:                                  301 milliSec
*   stream letto in un byte-array gestito dal programmatore: 817 milliSec
* */
public class MainClass {
    public static final int capacity = 1024;
    public static long time1;
    public static void main(String[] args) throws IOException {

        if(args.length < 1){
            System.err.println("\tUsage: MainClass filepath");
            System.exit(1);
        }

        File curr = new File(args[0]);

        if(!(curr.exists() && curr.isFile())){
            System.err.println("\tno such file with this filepath");
            System.exit(1);
        }

        //FileChannel con buffer indiretti
        try(ReadableByteChannel source =
                    Channels.newChannel(new FileInputStream(args[0]));
            WritableByteChannel dest =
                    Channels.newChannel (new FileOutputStream("out1.txt"))) {

            time1 = setTime();
            ByteBuffer byteBuffer1 = ByteBuffer.allocate(capacity);
            channelCopy (source, dest, byteBuffer1);
            System.out.print("FileChannel con buffer indiretti: ");
            getTime(time1);
        }

        //FileChannel con buffer diretti
        try(ReadableByteChannel source =
                    Channels.newChannel(new FileInputStream(args[0]));
            WritableByteChannel dest =
                    Channels.newChannel (new FileOutputStream("out2.txt"))) {

            time1 = setTime();
            ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(capacity);
            channelCopy (source, dest, byteBuffer2);
            System.out.print("FileChannel con buffer diretti: ");
            getTime(time1);
        }

        //FileChannel utilizzando l'operazione transferTo()
        try(    RandomAccessFile fromFile = new RandomAccessFile( args[0], "rw");
            FileChannel fromChannel = fromFile.getChannel();
                RandomAccessFile toFile = new RandomAccessFile("out3.txt", "rw");
            FileChannel toChannel = toFile.getChannel();) {

            time1 = setTime();
            long position = 0;
            long count = fromChannel.size();
            toChannel.transferFrom(fromChannel, position, count);
            System.out.print("FileChannel utilizzando l'operazione transferTo(): ");
            getTime(time1);
        }

        //Buffered Stream di I/O
        try( BufferedInputStream in = new BufferedInputStream( new FileInputStream (args[0]));
             BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream ("out4.txt"))){

            time1 = setTime();
            byte[] buffer = new byte[capacity];

            int read;
            while(
                    (read = in.read(buffer)) != -1 ){
                out.write(buffer,0, read);
                //out.flush();
            }
            System.out.print("Buffered Stream di I/O: ");
            getTime(time1);
        }

        //stream letto in un byte-array gestito dal programmatore
        try( FileInputStream in = new FileInputStream (args[0]);
             FileOutputStream out = new FileOutputStream ("out5.txt")){

            time1 = setTime();
            byte[] buffer = new byte[capacity];

            int read;
            while( (read = in.read(buffer)) != -1 ){
                out.write(buffer,0, read);
                //out.flush();
            }
            System.out.print("stream letto in un byte-array gestito dal programmatore: ");
            getTime(time1);
        }

    }

    public static void channelCopy(ReadableByteChannel r, WritableByteChannel w, ByteBuffer buffer) throws IOException {
        while (r.read (buffer) != -1) {
            //setto buffer per la lettura
            buffer.flip();

            while (buffer.hasRemaining())
                w.write (buffer);
            //tutti i dati sono stati letti e scaricati sul file

            // setto buffer per la scrittura
            buffer.clear();
        }
    }

    public static long setTime(){
        return System.currentTimeMillis();
    }

    public static void getTime(long time){
        long currTime = System.currentTimeMillis();
        System.out.println( currTime-time + " milliSec");
    }
}
