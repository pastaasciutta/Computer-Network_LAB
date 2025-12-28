import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {

    private final ThreadPoolExecutor TreadPool;

    public ThreadPool(){
        this.TreadPool = new ThreadPoolExecutor
                (0, 10, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    /** @param len file lenght */
    public void execute(String logpath, int len){

        int off=0;
        String[] someLines = new String[5];

        ReadTask task = new ReadTask(logpath, someLines, off, len);

        try{
            String[] lines = task.FileIS();

            /* una volta letto il contenudo del file in lines sposta il contenuto in someLines
            e passsalo al pool per svolgere la task di formattazione*/
            for (int i=0; i<lines.length; i++){
                int j=i%5;
                someLines[j] = lines[i];

                //eseguo il pool solo quando someLines è piena
                if (j==0 && i>0){
                    ReadTask currentTask = new ReadTask(logpath, someLines, ++off, len);
                    TreadPool.execute(currentTask);
                }
            }
        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }

    }

    public void shoutDown(){
        try{
            if (!TreadPool.awaitTermination(20, TimeUnit.SECONDS))
                TreadPool.shutdownNow();
        } catch (InterruptedException e){
            TreadPool.shutdownNow();
        }
    }
}
