import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Scrivere un programma in cui un contatore viene aggiornato da 20 scrittori
 * e il suo valore letto e stampato da 20 lettori.
 *
 * 1- Creare una Classe Counter che offre i metodi increment() e get()
 *    per incrementare e recuperare il valore di un contatore.
 * 2- Definire un task Writer che implementa Runnable e nel metodo
 *    run invoca il metodo increment di un oggetto Counter
 * 3- Definire un task Reader che implementa Runnable e nel metodo
 *    run invoca il metodo get di un oggetto Counter e lo stampa
 * 4- Definire una classe contenente il metodo main. Nel main viene creata
 *    un’istanza di Counter. Vengono quindi creati 20 oggetti di tipo Writer e
 *    20 oggetti di tipo Reader (a cui viene passato il riferimento
 *    all’oggetto counter nel costruttore). I task vengono quindi assegnati
 *    ad un threadpool (inviare al pool prima i writer e poi i reader)
 *    (suggerimento: usare un CachedThreadPool).
 * 5- Estendere la classe Counter fornita usando un oggetto di tipo ReentrantLock
 *    per garantire l’accesso in mutua esclusione alle sezioni critiche.
 * 6- Estendere la classe Counter usando al posto di ReentrantLock
 *    delle Read/Write Lock e confrontare l’intervallo di tempo richiesto
 *    dal threadpool per completare i task in questo caso col caso precedente
 *    (usare System.currentTimeMillis() per recuperare l’ora corrente,
 *    potete prendere un primo timestamp prima del ciclo di creazione dei task
 *    e il secondo timestamp dopo la terminazione del threadpool).
 * 7- (opzionale) Sostituire il threadpool di tipo CachedThreadPool con
 *    un FixedThreadPool, al variare del numero di thread (es. 1 ,2, 4) verificare
 *    l’intervallo di tempo richiesto dal threadpool per completare i task
 */
public class MainClass {
    public static void main(String[] args) {
        final int n_writer = 20, n_reader = 20;

        //4-
        // Counter counter= new Counter();
        //5-
        // Counter counter= new Counter_ReentrantLock();
        //6-
        Counter counter= new Counter_ReadWriteLock();

        //creating threadpool
        //ExecutorService threadpool = Executors.newCachedThreadPool();
        ExecutorService threadpool = Executors.newFixedThreadPool(4);

        long starting_time = System.currentTimeMillis();

        for(int i=0; i<n_writer; i++)
            threadpool.execute(new Writer(counter));

        for(int i=0; i<n_reader; i++)
            threadpool.execute(new Reader(counter));

        //shutting threadpool down
        threadpool.shutdown();
        try{
            while(!threadpool.awaitTermination(3, TimeUnit.SECONDS))
                threadpool.shutdownNow();
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        long finishing_time = System.currentTimeMillis();
        System.out.printf("Tempo impiegato: %dms\n", finishing_time-starting_time);
    }
}
