package src;

public class Reader implements Runnable{

    //non metto = new counter() perche altrimenti inizializzerei la variabile (che invece mi serve per buttarci roba dal main)
    private Counter counter;
    public Reader(Counter counter){
        this.counter = counter;
    }
    public void run(){
        System.out.println(counter.get());
    }
}