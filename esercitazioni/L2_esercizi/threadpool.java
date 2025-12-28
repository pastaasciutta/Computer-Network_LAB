/*Esercizio 1 - Threadpool
Nella sala biglietteria di una stazione sono presenti 5 emettitrici automatiche dei biglietti.
Nella sala non possono essere presenti più di 10 persone in attesa di usare le emettitrici.

Scrivere un programma che simula la situazione sopra descritta.

 La sala della stazione viene modellata come una classe JAVA.
 Uno dopo l’altro arrivano 50 viaggiatori (simulare un intervallo di 50 ms con Thread.sleep).

 ogni viaggiatore viene simulato da un task, la prima operazione consiste nello stampare 
 “Viaggiatore {id}: sto acquistando un biglietto”, aspettare per un intervallo di tempo 
 random tra 0 e 1000 ms e poi stampa “Viaggiatore {id}: ho acquistato il biglietto”.

 I task vengono assegnati a un numero di thread pari al numero delle emettitrici

 Il rispetto della capienza massima della sala viene garantita dalla coda gestita dal threadpool.
 I viaggiatori che non possono entrare in un certo istante perché la capienza massima è stata 
 raggiunta abbandonano la stazione (il programma main stampa quindi “Traveler no.  {i}: sala esaurita”.

Suggerimento: usare un oggetto ThreadPoolExecutor in cui il numero di thread è pari al numero degli sportelli*/

package lab_reti.L2_esercizi;

import java.lang.Math;
import java.util.InputMismatchException;
import java.util.concurrent.*;


public class threadpool{
    public static void main(String args[]){
        final int num_voyager = 50;
        final int max_voyager_attendance_time = 50;

        sala s = new sala();
        for(int i=0; i<num_voyager; i++){

            voyager v = new voyager(i); 
            try {
                s.execute(v);
            } catch (RejectedExecutionException e) {
                //eccezione specifica > task rejettato 
                System.out.printf("Traveler no. %d: sala esaurita\n", i);
            }
            try {
                Thread.sleep(max_voyager_attendance_time);
            } catch (InterruptedException e) {
                //interruzione del thread che sto facendo dormire perche boh ne sono partiti altri o cazzate simmili
                System.out.println(e.getMessage());
            }
        }
        s.end();
    }
}

//viaggiatore simulato da un task (le task si implementano con runnable)
public class voyager implements Runnable{

    private int id;
    public voyager(int id){
        this.id = id;
    }

    public void run(){
        Random rand = new Random();
        System.out.printf("Viaggiatore %d: sto acquistando un biglietto\n", id);

        long waiting = rand.nextInt(1000);
        try {
            Thread.sleep(waiting);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        System.out.printf("Viaggiatore %d: ho acquistato il biglietto\n", id);
    }
}

public class sala{
    private ThreadPoolExecutor ExecutorService;//?
    /*le prime due variabili rappresentano il minimo e massimo numero di tread (le emettitrici)
    3 4 (il tempo di?) il 5 il nuumero di task in coda (viaggiatori)*/
    public sala(){
        ExecutorService service = new ThreadPoolExecutor(5, 5, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));
    }
        public void execute(voyager vg){
        //voyager sta usando l'emettirice
        service.execute(vg);
    }
    public void end(){
        service.shutdown();
    }
}