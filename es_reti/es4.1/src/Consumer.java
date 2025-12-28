public class Consumer implements Runnable {

    private boolean e;
    private Dropbox dropbox;
    public Consumer(boolean e, Dropbox dropbox){
        this.e = e;
        this.dropbox = dropbox;
    }

    @Override
    public void run() {
        this.dropbox.take(e);
    }
}
