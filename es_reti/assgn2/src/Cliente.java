import java.util.Random;

public class Cliente implements Runnable{
    final int ticket;
    final int max_time = 3000;
    int c_time = 0;
    public Cliente(int ticket){
        this.ticket = ticket;
    }

    @Override
    public void run() {
        System.out.println(ticket +" :nuovo Cliente sta acquistando ticket");
        Random rand = new Random();
        c_time = rand.nextInt(max_time);
        try{
            Thread.sleep(c_time);
            System.out.println(ticket + " :il Cliente col ticket ha finito l'operazione");
        } catch (InterruptedException e){
            System.out.println(ticket + " :il Cliente col ticket ha finito prima l'operazione");
        }
    }
}
