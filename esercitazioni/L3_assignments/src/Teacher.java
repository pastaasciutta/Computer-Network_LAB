import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;

public class Teacher implements Runnable {

    int id;
    private final Lock write = ComputerRoom.Room.writeLock();
    public Teacher(int id) { this.id = id; }

    public void run() {
        final int tmax = 3000;
        final int attesa = 2000;
        final int kmax = 5;
        //numero di volte che l'insegnante richiede di entrare nel laboratorio compreso fra 1 e k
        int k = ThreadLocalRandom.current().nextInt(1, kmax);

        for (int j = 0; j < k; j++) {
            //cerca di accedere all'aula (accede sicuramente k volte)
            if (write.tryLock()){
                //successo
                System.out.printf("[%d professore] accede all'aula\n{%d° volta}\n", id, j+1);
                //tempo randomico che l'insegnante impiega per usare il computer
                int uso_aula = ThreadLocalRandom.current().nextInt(1, tmax);
                try {
                    Thread.sleep(uso_aula);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                } finally {
                    write.unlock();
                }
                System.out.printf("[%d professore] lascia l'aula dopo %dms\n", id, uso_aula);
            } else j--; //insuccesso

            //simulazione del tempo d'attesa tra una richiesta d'accesso e l'altra
            try{
                Thread.sleep(attesa);
            } catch (InterruptedException e){
                System.out.println(e.getMessage());
            }
        }
    }
}