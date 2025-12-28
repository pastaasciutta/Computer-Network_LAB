import java.util.LinkedList;

public class DirectoryList {

    private LinkedList<String> list;
    private int lenght;
    private boolean done;

    public DirectoryList(){
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
    }

    public synchronized void setDone(){ done = true; }

    public synchronized boolean isEmpty(){ return lenght==0 ? true : false; }
}
