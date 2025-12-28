import java.io.File;

/**
 * Scrivere un programma che conti le occorrenze dei caratteri alfabetici (lettere dalla “A” alla “Z”)
 * in un insieme di file di testo. Il programma prende in input una serie di percorsi di file testuali
 * e per ciascuno di essi conta le occorrenze dei caratteri, ignorando eventuali caratteri non alfabetici
 * (come per esempio le cifre da 0 a 9). Per ogni file, il conteggio viene effettuato da un apposito task
 * e tutti i task attivati vengono gestiti tramite un pool di thread.
 * I task registrano i loro risultati parziali all’interno di una ConcurrentHashMap.
 * Prima di terminare, il programma stampa su un apposito file di output il numero di occorrenze di ogni carattere.
 *
 * Il file di output contiene una riga per ciascun carattere ed è formattato come segue:
 *
 * *carattere1*,*numero di occorrenze*
 * *carattere2*,*numero di occorrenze*
 * ...
 * *caratteren*,*numero di occorrenze*
 *
 * esempio di file di output:
 *
 * a,1281
 *
 * b,315
 *
 * c,261
 *
 * d,302
 * */
public class MainClass {
    public static void main(String[] args) {
        //input una serie di percorsi di file testuali or una directory con dentro i file
        if(args.length == 0 || (new File(args[0])).isDirectory() ) {
            System.err.println("Usage: MainClass filepath .. filepath\n"
                    + "\tfilepath \t percorso al file, fino a n filepath \n "
                    + "\n\nExample: MainClass /User/Desktop/Cartella1/file1 /User/Desktop/file2");
            System.exit(1);
        }

        //creo e inizalizzo la cuncurrent HashMap in cui salvo le occorenze delle lettere dell'alfabeto
        AlphabetCHM Alphabet = new AlphabetCHM();
        Alphabet.initialize();

        //creo threadpool che riempirà Alphabet dopo aver letto tutti i file
        Pool pool = new Pool(args.length, Alphabet);
        pool.execute(args);
        pool.shoutDown();

        Thread NewFileCreator = new Thread(new WriteTask(Alphabet));
        NewFileCreator.start();
    }
}
