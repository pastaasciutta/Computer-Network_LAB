import java.io.File;
import java.util.Scanner;
import java.util.InputMismatchException;

public class MainClass {
    public static void main(String[] args) {

        String path;
        int tnum; //thread

        System.out.println("Inserisci il percorso del file");
        System.out.println("Inserisci il numero di thread");

        try{
            Scanner scanner = new Scanner(System.in);
            path = scanner.nextLine();
            tnum = scanner.nextInt();

            File file = new File(path);
            SynchronizedList list = new SynchronizedList();

            //attivo thread produttore
            Producer producer = new Producer(list,file);
            Thread tproducer = new Thread(producer);
            tproducer.start();

            try {
                tproducer.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //attivo thread consumatori
            for(int i = 0; i< tnum; i++){
                Consumer consumer = new Consumer(list);
                Thread tconsumer = new Thread(consumer);
                tconsumer.start();
            }

        } catch (InputMismatchException e) {
            System.out.println("inserimento errato");
        }
    }
}
