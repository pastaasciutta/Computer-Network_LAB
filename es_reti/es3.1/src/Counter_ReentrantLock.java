import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Counter_ReentrantLock extends Counter{
    private final Lock reentrantLock = new ReentrantLock();

    @Override
    public void increment() {
        reentrantLock.lock();
        super.increment();
        reentrantLock.unlock();
    }

    @Override
    public int get() {
        reentrantLock.lock();
        int curr = super.get();
        reentrantLock.unlock();
        return curr;
    }
}
