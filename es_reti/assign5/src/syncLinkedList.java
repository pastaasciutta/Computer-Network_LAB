import java.util.LinkedList;

// linkedList sincronizzata
public class syncLinkedList {

    private LinkedList<String> list;
    private int lenght;
    private boolean done;

    public syncLinkedList(){
        this.list = new LinkedList<String>();
        this.lenght = 0;
        this.done = false;
    }

    //legge e rimuove il primo elemento della lista
    public synchronized String getHead(){
        while (!done && lenght == 0){
            try{
                wait();
            } catch (InterruptedException e) { }
        }
        String file = list.poll();
        lenght--;
        notifyAll();
        return file;
    }

    //aggiunge nuovo elemento in coda
    public synchronized void Add(String f){
        list.add(f);
        lenght ++;
        notifyAll();
    }

    public void setDone(){
        done = true;
    }
}
