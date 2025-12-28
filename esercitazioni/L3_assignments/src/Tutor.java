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

        new ComputerRoom(); //(è importnte inizializzare il mio array di lock altrimenti non esiste per i threads usati di seguito)
        //faccio partire gli n thread
        System.out.println("    apertura aula computer\n(nb: ogni utente ha un id unico)\n            ...");
        for (int i = 1; i <= n; i++) {
            if (np > 0){
                tutor.execute(new Teacher(i));
                np--;
            } else {
                if (nt > 0){
                    tutor.execute(new ThesisWorker(i));
                    nt--;
                } else {
                    if (ns > 0){
                        tutor.execute(new Student(i));
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