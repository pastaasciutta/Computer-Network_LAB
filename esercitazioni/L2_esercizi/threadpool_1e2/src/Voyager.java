import java.util.Random;

//viaggiatore simulato da un task (le task si implementano con runnable)
public class Voyager implements Runnable{

    private int id;
    public Voyager(int id){
        this.id = id;
    }
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