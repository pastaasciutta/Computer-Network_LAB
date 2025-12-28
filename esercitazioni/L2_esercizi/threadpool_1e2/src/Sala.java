import java.util.concurrent.*;

public class Sala{
    private ThreadPoolExecutor service;
    /*le prime due variabili rappresentano il minimo e massimo numero di tread (le emettitrici)
    3 4 (il tempo di?) il 5 il nuumero di task in coda (viaggiatori)*/
    public Sala(){
        this.service = new ThreadPoolExecutor(5, 5, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));
    }

    public void execute(Voyager vg){
        //Voyager sta usando l'emettirice
        service.execute(vg);
    }

    public void chiudi_sala(){
        try {
            //service.shutdown() termina una volta che tutti i thread hanno terminato il task
            service.shutdown();
            /*nel caso in cui i thread non terminassero per boh uso awaitTermination
              aspettando 3sec dopo di che staccoh tutto a prescinde con shutdownNow*/
            if(service.awaitTermination(3000, TimeUnit.MILLISECONDS))
                service.shutdownNow();
        } catch (InterruptedException e) {
            /*nel caso in cui ho eccezioni stampo il messaggio del tipo di eccezione
              e spengo subito il threadpool anche se i thread sono ancora attivi*/
            System.out.println(e.getMessage());
            service.shutdownNow();
        }
    }
}