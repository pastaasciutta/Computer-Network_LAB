import java.util.concurrent.atomic.AtomicBoolean;

public class ComputerRoomRWMonitor {

    private int readcount;   // n lettori in lettura
    private AtomicBoolean writecount = new AtomicBoolean(false);  // dice se lo scrittore è in scrittura o meno

    public ComputerRoomRWMonitor() {
        readcount = 0;
    }

    public synchronized boolean startRead () {
        //se ci sono scrittori attivi aspetta
        while (writecount.get()) {
            try {
                wait();
            } catch (Exception e) {};
        }
        //appena non ci sono piu scrittori il numero dei lettori viene incrementato (c'è un nuovo lettore)
        readcount++;
        return true;
    }

    public synchronized void endRead () {
        readcount--;
        //se non ci sono piu lettoi attivi notifica tutti i thread (saranno solo scrittori)
        if (readcount == 0) {
            notifyAll();
        }
    }

    public synchronized boolean startWrite (Teacher T) {
        //aspetta se ci sono lettori o scrittori attivi
        while (readcount > 0 || !writecount.compareAndSet(false, true)) {
            try {  wait(); } catch (Exception e) {};  // wait for notify()
        }
        //successo della write
        System.out.printf("[%d Professore] accede all'aula\n{%d° volta}\n\n", T.cod, T.getJ());
        int c = new InsideComputerRoom().use(T);
        System.out.printf("[%d Proefessore] lascia l'aula dopo %dms\n\n", T.cod, c);
        return true;
    }

    public synchronized void endWrite () {
        writecount.compareAndSet(true, false);
        notifyAll();
    }
}

