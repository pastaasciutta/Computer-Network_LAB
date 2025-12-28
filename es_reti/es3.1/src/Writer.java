public class Writer implements Runnable{
    private Counter counter;
    public Writer(Counter counter) { this.counter = counter;}

    @Override
    public void run() {
        counter.increment();
    }
}
