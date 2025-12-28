import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Sala {

    private final ThreadPoolExecutor threadpoolEmettitrici;
    //costruttore
    public Sala(){
        this.threadpoolEmettitrici = new ThreadPoolExecutor(5, 5, 1,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));
    }

    //thread exectuing task (Voyager using emettirice)
    public void execute(Voyager vg){
        this.threadpoolEmettitrici.execute(vg);
    }

    //ESERCIZIO 2
    //closing Threadpool
    public void close_sala(){
        try {
            //threadpoolEmettitrici.shutdown() termina una volta che tutti i thread hanno terminato il task
            threadpoolEmettitrici.shutdown();

            if(threadpoolEmettitrici.awaitTermination(3000, TimeUnit.MILLISECONDS))
                threadpoolEmettitrici.shutdownNow();

        } catch (InterruptedException e) {

            System.out.println(e.getMessage());
            threadpoolEmettitrici.shutdownNow();
        }
    }
}
