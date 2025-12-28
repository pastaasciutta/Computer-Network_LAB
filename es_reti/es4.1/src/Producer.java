import java.util.Random;

public class Producer implements Runnable{

    private Dropbox dropbox;
    public Producer(Dropbox dropbox){
        this.dropbox = dropbox;
    }

    @Override
    public void run() {
        Random random = new Random();
        int num = random.nextInt(100);
        this.dropbox.put(num);
    }
}
