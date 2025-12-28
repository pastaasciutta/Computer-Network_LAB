import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThesisWorker implements Runnable {

    int mat;
    private final Lock read = ComputerRoom.Room.readLock();
    private final ArrayList<ReentrantLock> Computer = ComputerRoom.Computers;
    public ThesisWorker(int mat) { this.mat = mat; }

    public void run() {
        final int tmax = 2000;
        final int attesa = 4000;
        final int kmax = 5;
        //computer iesimo che il tesista ha bisogno di usare
        int i = mat % (ComputerRoom.N_comp-1);
        //numero di volte che il tesista richiede di entrare nel laboratorio compreso fra 1 e k
        int k = ThreadLocalRandom.current().nextInt(1, kmax);

        for (int j = 0; j < k; j++){
            //il tesista cerca di accedere all'aula (accede sicuramente k volte)
            if (read.tryLock()){
                //verifica che il computer iesimo sia libero
                if (Computer.get(i).tryLock()){
                    //in caso in cui sia libero lo occupa
                    System.out.printf("[%d tesista] accede al computer %d\n{%d° volta}\n", mat, i, j+1);
                    //tempo randomico che il tesista mat-esimo impiega per usare il computer
                    int uso_computer = ThreadLocalRandom.current().nextInt(1, tmax);
                    try {
                        Thread.sleep(uso_computer);
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    } finally {
                        Computer.get(i).unlock();
                        read.unlock();
                    }
                    System.out.printf("[%d tesista] lascia l'aula dopo %dms\n", mat, uso_computer);
                }
            } else j--; //in caso in cui il computer non sia libero decremento j (per assicurarmi che usi il computer k volte)

            //simulazione del tempo d'attesa tra una richiesta d'accesso e l'altra
            try{
                Thread.sleep(attesa);
            } catch (InterruptedException e){
                System.out.println(e.getMessage());
            }
        }
    }
}