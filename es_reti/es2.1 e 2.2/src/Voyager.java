import java.util.Random;

//viaggiatore simulato da una task
public class Voyager implements Runnable{

    private int id;
    //constructor
    public Voyager(int id){
        this.id = id;
    }

    //runnable
    public void run(){
        Random rand = new Random();
        System.out.printf("Viaggiatore %d: sto acquistando un biglietto\n", id);

        long waiting = rand.nextInt(1000);
        try {
            Thread.sleep(waiting);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        System.out.printf("Viaggiatore %d: ho acquistato il biglietto\n", id);
    }
}