public class Reader implements Runnable{
    private Counter counter;
    public Reader(Counter counter){ this.counter = counter;}

    @Override
    public void run() {
        counter.get();
    }
}
