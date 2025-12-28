package src;

public class Writer implements Runnable{
    
    //non metto = new counter() perche altrimenti inizializzerei la variabile (che invece mi serve per buttarci roba dal main)
    private Counter counter;
    public Writer(Counter counter){
        this.counter = counter;
    }

    public void run(){
        counter.increment();
    }
}