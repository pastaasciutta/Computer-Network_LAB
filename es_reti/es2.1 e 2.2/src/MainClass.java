/*
ESERCIZIO 1 - Threadpool
Nella Sala biglietteria di una stazione sono presenti 5 emettitrici automatiche dei biglietti.
Nella Sala non possono essere presenti più di 10 persone in attesa di usare le emettitrici.

Scrivere un programma che simula la situazione sopra descritta.

 La sala della stazione viene modellata come una classe JAVA.
 Uno dopo l’altro arrivano 50 viaggiatori (simulare un intervallo di 50 ms con Thread.sleep).

 ogni viaggiatore viene simulato da un task, la prima operazione consiste nello stampare
 “Viaggiatore {id}: sto acquistando un biglietto”, aspettare per un intervallo di tempo
 random tra 0 e 1000 ms e poi stampa “Viaggiatore {id}: ho acquistato il biglietto”.

 I task vengono assegnati a un numero di thread pari al numero delle emettitrici

 Il rispetto della capienza massima della sala viene garantita dalla coda gestita dal Threadpool.
 I viaggiatori che non possono entrare in un certo istante perché la capienza massima è stata
 raggiunta abbandonano la stazione (il programma main stampa quindi “Traveler no.  {i}: sala esaurita”.

Suggerimento: usare un oggetto ThreadpoolExecutor in cui il numero di thread è pari al numero degli sportelli

ESERCIZIO 2
 Estendi il programma dell’Esercizio 1 gestendo la terminazione del threadpool.
 Dopo l’arrivo dell’ultimo viaggiatore e l’invio del corrispondente task al threadpool, terminare il threadpool.
 */

import java.util.concurrent.RejectedExecutionException;

public class MainClass {
    public static void main(String args[]){
        final int num_voyager= 50;
        final int attendance_time= 50; //(ms)
        int num_voyagers_served= 0;

        Sala sala= new Sala();

        for (int i=0; i<=num_voyager; i++){
            Voyager voyager= new Voyager(i);

            try{
                sala.execute(voyager);
                num_voyagers_served++;
            } catch (RejectedExecutionException e){
                System.out.printf("Traveler no. %d: sala esaurita\n", i);
            }

            try{
                Thread.sleep(attendance_time);
            } catch (InterruptedException e){
                System.out.println(e.getMessage());
            }
        }
        //ESERCIZIO 2
        sala.close_sala();

        System.out.printf("%d Viaggiatori serviti\n", num_voyagers_served);
    }
}
