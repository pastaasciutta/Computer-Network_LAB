import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Scrivere un programma che dato in input una lista di directories,
 * comprima tutti i file in esse contenuti, con l'utility gzip
 *
 * ipotesi semplificativa:  zippare solo i file contenuti nelle directories passate in input,
 * non considerare ricorsione su eventuali sottodirectories
 *
 * il riferimento ad ogni file individuato viene passato ad un task, che deve essere eseguito in un threadpool
 * individuare nelle API JAVA la classe di supporto adatta per la compressione
 *
 * NOTA: l'utilizzo dei threadpool è indicato, perchè I task presentano un buon mix tra I/O e computazione
 *            I/O heavy: tutti i file devono essere letti e scritti
 *           CPU-intensive: la compressione richiede molta computazione
 *
 * facoltativo: comprimere ricorsivamente i file in tutte le sottodirectories
 */
public class MainClass {
    public static void main(String[] args) {

        if (args.length == 0)
            System.exit(-1);

        //creao directoryList
        DirectoryList Directories = new DirectoryList();

        Initialization task = new Initialization(Directories, args);
        Thread Producer = new Thread(task);
        Producer.start();

        TreadpoolFactory treadpoolFactory = new TreadpoolFactory(Directories, args.length);
        treadpoolFactory.execute();
        treadpoolFactory.shoutDown();
    }
}
