import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Tutor {
    ExecutorService tutor;
    //il tutor gestisce n thread (studenti, tesisti e prof che cercano di entrare)
    public Tutor(int n){
        this.tutor = Executors.newFixedThreadPool(n);
    }

    public void queue(int n, int np, int nt, int ns){

        InsideComputerRoom Inside = new InsideComputerRoom();
        ComputerRoomRWMonitor Outside = new ComputerRoomRWMonitor();

        //faccio partire gli n thread
        System.out.println("    apertura aula computer\n(nb: ogni utente ha un id unico)\n            ...");
        for (int i = 1; i <= n; i++) {
            if (np > 0){
                tutor.execute(new Teacher(i, Outside));
                np--;
            } else {
                if (nt > 0){
                    tutor.execute(new ThesisWorker(i, Inside, Outside, Inside.Computers));
                    nt--;
                } else {
                    if (ns > 0){
                        tutor.execute(new Student(i, Inside, Outside, Inside.Computers));
                        ns--;
                    }
                }
            }
        }
    }

    public void closeComputerRoom(){
        tutor.shutdown();
        try {
            if (!tutor.awaitTermination(60, TimeUnit.SECONDS)) {
                tutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            tutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("            ...\n   chiusura aula computer");
    }
}
