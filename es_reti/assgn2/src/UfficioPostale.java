import java.util.concurrent.*;

public class UfficioPostale {
    final private int sportelli = 4;
    private BlockingQueue<Cliente> SalaGrande;
    /* oppure potevo usare FixedTreadpool visto che utilizzo solo i thread del core
       + ArrayBlockingQueue a parte per capienza sal piccola */
    private ThreadPoolExecutor SalaPiccola;
    int n_clienti;

    public UfficioPostale(int n_clienti, int capienza_SalaPiccola){
        this.n_clienti = n_clienti;
        this.SalaGrande = new ArrayBlockingQueue<>(n_clienti);
        //core size = 0 di modo che 'il dipendente chida lo sportello se non si presenta nessuno entro il keepAliveTime'
        this.SalaPiccola = new ThreadPoolExecutor(0, sportelli, 5000,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(capienza_SalaPiccola));
    }

    public void Apertura() {
        //I clienti entrano nella sala grande con ticket iesimo
        for(int i=1; i<=n_clienti; i++)
            SalaGrande.add(new Cliente(i));

        while(!SalaGrande.isEmpty()){
            try{
                //flusso continuo di clienti
                //peek: Retrieves, but does not remove, the head of this queue
                Cliente cliente_corrente = this.SalaGrande.peek();
                //provo ad inserire il cliente della prima sala nella seconda
                Entrata_SalaPiccola(cliente_corrente);
                SalaGrande.remove();
            } catch (RejectedExecutionException e){
                continue;
            }
        }
    }

    public void Entrata_SalaPiccola(Cliente cliente) {
        SalaPiccola.execute(cliente);
    }

    public void Chiusura(){
        try{
            if (!SalaPiccola.awaitTermination(5, TimeUnit.SECONDS))
                SalaPiccola.shutdown();
        } catch (InterruptedException e){
            System.out.println(e.getMessage());
        } finally {
            SalaPiccola.shutdownNow();
        }
    }
}
