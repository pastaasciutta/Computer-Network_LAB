import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Pool {

    private final ThreadPoolExecutor TreadPool;
    private AlphabetCHM finalMap;
    /** @param n proportional to Max Pool size */
    public Pool(int n, AlphabetCHM finalMap){
        this.TreadPool = new ThreadPoolExecutor
                (0, (n/2 +1), 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        this.finalMap = finalMap;
    }

    public void execute(String[] args){
        for (String path : args){
            ReadTask task = new ReadTask(path, finalMap);
            this.TreadPool.execute(task);
        }
    }

    public void shoutDown(){
        try{
            if (!TreadPool.awaitTermination(5, TimeUnit.SECONDS))
                TreadPool.shutdownNow();
        } catch (InterruptedException e){
            TreadPool.shutdownNow();
        }
    }
}