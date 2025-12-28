import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Student implements Runnable{

    int mat;
    private final Lock read = ComputerRoom.Room.readLock();
    private final ArrayList<ReentrantLock> Computer = ComputerRoom.Computers;
    public Student(int mat){
        this.mat = mat;
    }

    public void run(){
        final int tmax = 1000;
        final int attesa = 6000;
        final int kmax = 5;
        //indice iesimo dell'array di computer
        int i;
        //numero di volte che lo studente richiede di entrare nel laboratorio compreso fra 1 e k
        int k = ThreadLocalRandom.current().nextInt(1, kmax);

        for (int j = 0; j < k; j++) {
            //lo studente cerca di accedere all'aula (accede sicuramente k volte)
            if (read.tryLock()){
                //cerca il primo computer libero
                for (i = 0; Computer.get(i).isLocked() && i < ComputerRoom.N_comp; i++) {;}
                //cerca di accedere al computer 'libero'
                if (Computer.get(i).tryLock()){
                    //in caso di successo
                    System.out.printf("[%d studente] accede al computer %d\n{%d° volta}\n", mat, i, j+1);
                    //tempo randomico che lo studente mat-esimo impiega per usare il computer
                    int uso_computer = ThreadLocalRandom.current().nextInt(1, tmax);
                    try {
                        Thread.sleep(uso_computer);
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    } finally {
                        Computer.get(i).unlock();
                        read.unlock();
                    }
                    System.out.printf("[%d studente] lascia l'aula dopo %dms\n", mat, uso_computer);
                }
            } else j--; /*in caso di insuccesso decremento l'indice j
            (che conta quante volte lo studente riesce ad accedere al computer)*/

            //simulazione del tempo d'attesa tra una richiesta d'accesso e l'altra
            try{
                Thread.sleep(attesa);
            } catch (InterruptedException e){
                System.out.println(e.getMessage());
            }
        }
    }
}