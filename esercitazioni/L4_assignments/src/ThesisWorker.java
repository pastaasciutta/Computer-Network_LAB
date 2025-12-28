import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThesisWorker implements Runnable{

    int mat, i, j;
    private final InsideComputerRoom Room;
    private final ComputerRoomRWMonitor RWMonitor;
    private final ArrayList<AtomicBoolean> Computer;

    public ThesisWorker(int mat, InsideComputerRoom Room, ComputerRoomRWMonitor RWMonitor, ArrayList<AtomicBoolean> C){ //room inizializzata e passata per riferimento da tutor
        this.mat = mat;
        this.Room = Room;
        this.RWMonitor = RWMonitor;
        this.Computer = C;
    }

    public void run(){
        final int attesa = 6000;
        final int kmax = 5;

        int n = InsideComputerRoom.N_comp;
        //indice computer specifico richiesto dal tesista matesimo
        this.i = mat % n;
        j = 0;

        //numero di volte che lo studente richiede di entrare nel laboratorio (compreso fra 1 e kmax)
        int k = ThreadLocalRandom.current().nextInt(1, kmax);

        //accede al lab k volte
        while (j < k) {
            //cerca di accedere al lab
            if(RWMonitor.startRead()) {
                //controlla che il computersia libero
                if (Computer.get(i).get()) {
                    try{
                        //cerca di accedere al computer
                        Room.UsingComputer(this);
                        RWMonitor.endRead();
                        //simulazione del tempo d'attesa tra una richiesta d'accesso e l'altra
                        Thread.sleep(attesa);
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    }
                    //incremento j solo nel caso in cui l'accesso al computer ha successo
                    j++;
                } else
                    RWMonitor.endRead();
            }
        }
    }

    public String getName(){
        return "Tesista";
    }

    public int getI(){
        return i;
    }

    public int getJ(){
        return j+1;
    }
}
