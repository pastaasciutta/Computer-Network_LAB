package src;

import java.util.concurrent.locks.ReentrantLock;

public class Counter{

    protected ReentrantLock lock = new ReentrantLock();
    private int contatore = 0;
    
    public void increment(){
        lock.lock();
        contatore++;
        lock.unlock();
    }

    public int get(){
        lock.lock();
        int temp = contatore;
        lock.unlock();
        return temp;
    }
}
