import java.io.File;
import java.util.ArrayList;

/* * Si scriva un programma JAVA che
 * - riceve in input un filepath che individua una directory D
 * - stampa le informazioni del contenuto di quella directory e ricorsivamente
 *   di tutti i file contenuti nelle sottodirectory di D
 *
 * Il programma deve essere strutturato come segue:
 * - attiva un thread produttore ed un insieme di k thread consumatori
 * - il produttore comunica con i consumatori mediante una coda
 * - il produttore visita ricorsivamente la directory data ed eventualmente
 *   tutte le sottodirectory e mette nella coda il nome di ogni directory individuata
 * - i consumatori prelevano dalla coda i nomi delle directories e stampano il loro contenuto (nomi dei file)
 * - la coda deve essere realizzata con una LinkedList.
 *   Ricordiamo che una Linked List non è una struttura thread-safe.
 *   Dalle API JAVA “Note that the implementation is not synchronized.
 *   If multiple threads access a linked list concurrently, and at least one of the threads modifies
 *   the list structurally, it must be synchronized externally”
 **/
public class MainClass {
    static final int k = 7;

    public static void main(String[] args){

        if (args.length == 0){
            //System.out.println("aaaaaaa");
            System.exit(-1);
        }

        File startDirectory = new File(args[0]);

        if (!startDirectory.exists()){
            System.out.println("non esiste nessun file al path" + args[0]);
            System.exit(-1);
        }

        if (!startDirectory.isDirectory()){
            System.out.println("il file iniziale non è una directory");
            System.exit(-1);
        }

        syncLinkedList queue = new syncLinkedList();

        //creo e avvio produttore
        Thread P = new Thread(new Producer(queue, startDirectory));
        P.start();

        //creo e avvio consumatore
        ArrayList<Thread> Consumers = new ArrayList<>(k);

        for(int j=0; j<k; j++){
            Thread a = new Thread(new Consumer(j, queue));
            Consumers.add(a);
            a.start();
        }

        //chiudo threads
        try{
            P.join();
        } catch (InterruptedException e){
            P.interrupt();
            e.printStackTrace();
        }

        for(Thread i: Consumers){
            try{
                i.join();
            } catch (InterruptedException e){
                i.interrupt();
                e.printStackTrace();
            }
        }
    }
}
