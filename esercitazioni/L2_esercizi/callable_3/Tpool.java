//per usare Executors.newFixedThreadPool
import java.util.concurrent.Executors;
//per dichiararre la "variabile" thread di tipo ThreadPoolExecutor
import java.util.concurrent.ThreadPoolExecutor;
//per usare unita di misura del tempo in awaitTermination
import java.util.concurrent.TimeUnit;

import org.graalvm.compiler.hotspot.replacements.ThreadSubstitutions;

public class Tpool {
    final int n_thread = 5;
    final int awaitT = 3000;

    private ThreadPoolExecutor thread;
    public Tpool(){
        /*Fixed thread pool executor – Creates a thread pool that reuses
          a fixed number of threads to execute any number of tasks.
          If additional tasks are submitted when all threads are active,
          they will wait in the queue until a thread is available.
          It is the best fit for most off the real-life use-cases.*/
        this.thread = (ThreadPoolExecutor) Executors.newFixedThreadPool(n_thread);
    }

    public void calcolo(Power p){
        //assegno task a thread
        thread.calcolo(p);
    }

    public void fine_calcolo(){
        //se vuoi spiegaazioni su sta roba vai in ex_L2 > threadpool_1e2 > sala.java
        try {
            thread.shutdown();
            if(thread.awaitTermination(awaitT, TimeUnit.MILLISECONDS))
            thread.shutdownNow();
        } catch (Exception e) {
            System.out.println(e.getmMessage());
            thread.shutdownNow();
        }
    }
}
