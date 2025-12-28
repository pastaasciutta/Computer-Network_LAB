import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerThreadPool {

    private final static int N=16;
    private ThreadPoolExecutor pool;

    public ServerThreadPool(){
        this.pool = new ThreadPoolExecutor(0, N, 10,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(N));
    }

    public void execute( ServerDungeonAdventures task ){
        pool.execute(task);
    }

    public void shoutDown(){
        try{
            if (!pool.awaitTermination(10, TimeUnit.SECONDS))
                pool.shutdownNow();
        } catch (InterruptedException e){
            pool.shutdownNow();
        }
    }
}
