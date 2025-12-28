public class MainClass {
    public static void main(String[] args){
        Dropbox db = new Dropbox();

        Consumer c1 = new Consumer(true, db);
        Consumer c2 = new Consumer(false, db);

        Producer p = new Producer(db);

        new Thread(c1).start();
        new Thread(c2).start();
        new Thread(p).start();
    }
}
