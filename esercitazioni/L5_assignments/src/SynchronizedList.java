import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class SynchronizedList {

    // dichiaro licked list di file
    private final LinkedList<File> list;
    //hasDone è falsa se il producer è ancora attivo falsa altrimenti
    private AtomicBoolean hasDone;
    // n tiene traccia della lunghezza della lista
    private int n = 0;

    public SynchronizedList() {
        //inizializzo stato a falso e lista vuota
        hasDone = new AtomicBoolean(false);
        this.list = new LinkedList<>();
    }

    //inserisco elementi in testa alla lista
    /*nb sara sempre synchronized perche c'è solo un thread
    che effettua l'operazione di push, il Producer*/
    public void pushF(File f){
        list.push(f);
        n++;
    }
    //eliimino elementi dalla testa
    public synchronized File popF(){

        // se la lista è vuota
        while(n == 0){
            if(hasDone.get())
                // ed il producer ha finito di inserire directories ritorno null
                return null;
            else{
                // ed il producer non ha finito metto i consumatori in attesa
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // se la lista è non vuota
        n--;
        notifyAll();
        //rimuovo e ritorno la testa dopo aver decrementato la lunghezza della lista
        return list.pop();
    }

    //il producer ha finito di inserire directories
    public void listFilled(){
        hasDone.set(true);
    }

}

