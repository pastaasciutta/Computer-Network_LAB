import java.io.File;

public class Producer implements Runnable{

    private final SynchronizedList list;
    private final File file;

    public Producer(SynchronizedList list, File file) {
        this.list = list;
        this.file = file;
    }

    public void run() {
        list.pushF(file);
        //vedo le sub-directories
        exploreDir(list, file);
        list.listFilled();
    }

    public void exploreDir(SynchronizedList l, File f) {
        File[] files = f.listFiles();
        //se non ci sono piu file
        if(files == null)
            return;
        //altrimenti scoro array
        for(File t : files) {
            if(t.isDirectory()) {
                l.pushF(t);
                exploreDir(l,t);
            }
        }
    }
}
