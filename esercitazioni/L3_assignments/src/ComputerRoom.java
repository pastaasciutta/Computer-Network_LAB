import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ComputerRoom {

    final static int N_comp = 20;
    //un computer è l'iesimo elemento dell'array di lock
    public static ArrayList<ReentrantLock> Computers = new ArrayList(N_comp);
    public static ReentrantReadWriteLock Room = new ReentrantReadWriteLock(true);

    public ComputerRoom() {
        for (int i = 0; i < N_comp; i++)
            Computers.add(new ReentrantLock(true));
    }
}
