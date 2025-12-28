import java.util.concurrent.ThreadLocalRandom;

public class Teacher implements Runnable{

    int j, cod;
    private final ComputerRoomRWMonitor RWMonitor;

    public Teacher (int cod, ComputerRoomRWMonitor RWMonitor) {
        this.cod = cod;
        this.RWMonitor = RWMonitor;
    }

    @Override
    public void run() {
        final int attesa = 6000;
        final int kmax = 5;

        j = 0;

        //numero di volte che lo studente richiede di entrare nel laboratorio (compreso fra 1 e kmax)
        int k = ThreadLocalRandom.current().nextInt(1, kmax);

        //accede al lab k volte
        while (j < k) {
            //controlla che il computersia libero
            if (RWMonitor.startWrite(this)) {
                try {
                    RWMonitor.endWrite();
                    //simulazione del tempo d'attesa tra una richiesta d'accesso e l'altra
                    Thread.sleep(attesa);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
                //incremento j solo nel caso in cui l'accesso al computer ha successo
                j++;
            }
        }
    }

    public int getJ(){
        return j+1;
    }
}
