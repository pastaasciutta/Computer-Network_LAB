import java.util.concurrent.*;

public class UfficioPostale{

    protected BlockingQueue<Cliente> q_sala1;
    protected ThreadPoolExecutor q_sala2;

    public UfficioPostale(int clienti, int k){
        int sportelli = 4;
        int keepAliveTime = 30000;
        this.q_sala1 = new ArrayBlockingQueue<>(clienti);
        this.q_sala2 = new ThreadPoolExecutor(sportelli, sportelli, keepAliveTime, TimeUnit.SECONDS, new ArrayBlockingQueue<>(k));
    }

    public void sala1(int clienti){
    
        for (int i=1; i <= clienti; i++){
            //inserisco i clienti nella sala1 assegnandogli il ticket iesimo
            Cliente clnt = new Cliente(i);
            q_sala1.add(clnt);
        }

        /*finché sono presenti clienti*/
        while (!q_sala1.isEmpty()){
            try{
                //provo ad inserire il cliente della prima sala nella seconda
                sala2(this.q_sala1.peek());
                //se l'operazione ha successo lo faccio uscire dalla prima sala
                q_sala1.remove();
            } catch (RejectedExecutionException e){
                continue;
            }
        }
    }

    public void sala2(Cliente c){
        q_sala2.execute(c);
    }

    public void chiudi_ufficio(){
        q_sala2.shutdown();
        try {
            if (!q_sala2.awaitTermination(60, TimeUnit.SECONDS)) {
                q_sala2.shutdownNow();
            }
        } catch (InterruptedException e) {
            q_sala2.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}