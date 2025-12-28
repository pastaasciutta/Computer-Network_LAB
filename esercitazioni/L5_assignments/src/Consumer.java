import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

public class Consumer implements Runnable{

    private final SynchronizedList list;

    public Consumer(SynchronizedList list) {
        this.list = list;
    }

    public void run() {

        File dir = list.popF();

        while(dir != null) {
            File[] files = dir.listFiles();
            System.out.println("directory " + dir.getName());

            //iterator utile per rendere iterabile l'array files
            Iterator<File> iterator = Arrays.stream(files).iterator();
            while(iterator.hasNext()){
                File f = iterator.next();
                System.out.println(f.getName());
            }
            dir = list.popF();
        }
        //se il file = NULL allora il tread cessa di esistere
    }
}