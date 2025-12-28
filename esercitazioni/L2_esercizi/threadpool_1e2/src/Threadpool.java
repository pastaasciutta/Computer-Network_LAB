/*Esercizio 1 - Threadpool
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

Suggerimento: usare un oggetto ThreadpoolExecutor in cui il numero di thread è pari al numero degli sportelli*/

import java.util.InputMismatchException;
import java.util.concurrent.*;
import Sala.*;
import Voyager.*;

public class Threadpool{
    public static void main(String args[]){
        final int num_Voyager = 50;
        final int attendance_time = 50;

        Sala s = new Sala();
        for(int i=0; i<num_Voyager; i++){

            Voyager v = new Voyager(i); 
            try {
                s.execute(v);
            } catch (RejectedExecutionException e) {
                //eccezione specifica > task rejettato 
                System.out.printf("Traveler no. %d: Sala esaurita\n", i);
            }
            try {
                Thread.sleep(attendance_time);
            } catch (InterruptedException e) {
                //interruzione del thread che sto facendo dormire perche boh ne sono partiti altri o cazzate simmili
                System.out.println(e.getMessage());
            }
        }
        s.chiudi_sala();
    }
}

