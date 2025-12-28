import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class InsideComputerRoom {
    final static int N_comp = 20;
    public ArrayList<AtomicBoolean> Computers = new ArrayList(N_comp);

    public InsideComputerRoom() {
        //alloco e inizializzo l'array di bool che rappresentano i computer della sala (true libero, false occupato)
        for (int i = 0; i < N_comp; i++) {
            Computers.add(new AtomicBoolean(true));
        }
    }

    public synchronized void UsingComputer (Runnable S) throws InterruptedException{

        int i, j, id;
        String name;

        //preno paramietri che mi servono nelle stampe
        if (S instanceof ThesisWorker) {
            i = ((ThesisWorker) S).getI();
            j = ((ThesisWorker) S).getJ();
            id = ((ThesisWorker) S).mat;
            name = ((ThesisWorker) S).getName();
        } else if (S instanceof Student) {
            i = ((Student) S).getI();
            j = ((Student) S).getJ();
            id = ((Student) S).mat;
            name = ((Student) S).getName();
        } else {
            i = 0;
            j = 0;
            id = 0;
            name = "-";
        }
        //finchè il computer iesimo non è libero aspetta
        while (!Computers.get(i).compareAndSet(true, false)) {
            try {
                this.wait();
            } catch (InterruptedException e) { }
        }
        //in caso di risveglio e/o successo tesista/studente utilizza il computer
        System.out.printf("[%d %s] accede al computer %d\n{%d° volta}\n\n", id, name, i, j);
        int c = use(S);
        System.out.printf("[%d %s] lascia l'aula dopo %dms\n\n", id, name, c);

        //rimuovo la 'lock' e notifico gli altri threads
        Computers.get(i).set(true);
        this.notifyAll();
    }

    public int use(Runnable S){

        final int tmax = 1000;
        int uso_computer = ThreadLocalRandom.current().nextInt(1, tmax);

        try {
            Thread.sleep(uso_computer);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        return uso_computer;
    }
}