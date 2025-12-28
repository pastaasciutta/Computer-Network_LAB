import java.util.Random;

public class Cliente implements Runnable{

    final int serving_time = 1500;
    private final int ticket;

    public Cliente(int ticket){
        this.ticket = ticket;
    }
    
    public void run(){
        System.out.printf("nuovo Cliente sta acquistando ticket %d\n", ticket);
        Random rand = new Random();
        int time = rand.nextInt(serving_time);
        try {
            //il Cliente viene servito
            Thread.sleep(time);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        //il Cliente ha finito
        System.out.printf("il Cliente col %d ticket ha finito l'operazione dopo %d millisec\n", ticket, time);
    }
}
