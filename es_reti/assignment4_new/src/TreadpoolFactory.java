import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TreadpoolFactory {

    private DirectoryList list;
    private final ThreadPoolExecutor compressionThread;

    public TreadpoolFactory(DirectoryList list, int maxPoolsize){
        this.list = list;
        this.compressionThread = new ThreadPoolExecutor
                (0, maxPoolsize, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    public void execute(){
        while(!list.isEmpty())
            compressionThread.execute(new Compression(list));
    }

    public void shoutDown(){
        try{
            if (!compressionThread.awaitTermination(5, TimeUnit.SECONDS))
                compressionThread.shutdownNow();
        } catch (InterruptedException e){
            compressionThread.shutdownNow();
        }
    }
}
