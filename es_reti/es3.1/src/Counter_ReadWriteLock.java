import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Counter_ReadWriteLock extends Counter{
    private final ReadWriteLock ReadWriteLock = new ReentrantReadWriteLock();
    private final Lock WriteLock = ReadWriteLock.writeLock();
    private final Lock ReadLock = ReadWriteLock.readLock();

    @Override
    public void increment() {
        WriteLock.lock();
        super.increment();
        WriteLock.unlock();
    }

    @Override
    public int get() {
        ReadLock.lock();
        int curr = super.get();
        ReadLock.unlock();
        return curr;
    }
}
