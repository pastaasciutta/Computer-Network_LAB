package src;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Lock_ReadWriteLock {
    public static void main(String[] args) {
        final int n = 20;
        Counter counter = new Counter();

        ExecutorService pippo = Executors.newCachedThreadPool();

        for(int i=0; i<n; i++){
            pippo.execute(new Writer(counter));
            pippo.execute(new Reader(counter));
        }

        try {
            //pippo.shutdown() termina una volta che tutti i thread hanno terminato il task
            pippo.shutdown();
            /*nel caso in cui i thread non terminassero per boh uso awaitTermination
              aspettando 3sec dopo di che staccoh tutto a prescinde con shutdownNow*/
            if(pippo.awaitTermination(3000, TimeUnit.MILLISECONDS))
                pippo.shutdownNow();
        } catch (InterruptedException e) {
            /*nel caso in cui ho eccezioni stampo il messaggio del tipo di eccezione
              e spengo subito il threadpool anche se i thread sono ancora attivi*/
            System.out.println(e.getMessage());
            pippo.shutdownNow();
        }
    }
}