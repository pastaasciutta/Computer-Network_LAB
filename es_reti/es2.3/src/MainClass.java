/*Scrivere un programma che calcola le potenze di un numero n (esempio n=2)
da n2 a n50 e restituisce come risultato la somma delle potenze, ovvero:
 Result = n2 + n3 + … + n50

 Creare una classe Power di tipo Callable che riceve come parametri di ingresso
 il numero n  e un intero (l’esponente), stampa “Esecuzione {n}^{esponente} in {idthread}”
 e restituisce il risultato dell’elevamento a potenza (usare la funzione Math.pow() di Java
 https://docs.oracle.com/javase/8/docs/api/java/lang/Math.html#pow-double-double-)

 Creare una classe che nel metodo public static void main(String args[])
 crea un threadpool e gli passa i task Power

 I risultati restituiti dai task vengono recuperati e sommati e il risultato della somma
 viene stampato (usare una struttura dati, es. ArrayList per memorizzare gli oggetti di tipo
 Future restituiti dal threadpool in corrispondenza dell’invocazione del metodo submit)*/

import java.util.ArrayList;
import java.util.concurrent.*;

public class MainClass {
    public static void main(String[] args) {
        final double base= 2;
        final int last_exp= 50;
        double result= 0;
        
        //dichiaro arraylist in cui storare i risultati delle n esecuzioni della task power
        ArrayList<Future<Double>> Stored_powers = new ArrayList<Future<Double>>();
        //thread che eseguiranno task gestiti da threadpool
        ExecutorService Threadpool = Executors.newFixedThreadPool(5);

        //riempio L'arraylist
        for (int i=0; i<= last_exp; i++){
            Power p = new Power(base, i);
            Stored_powers.add(Threadpool.submit(p));
        }
        
        try{
            //scorro Stored_powers sommando gli elementi
            for (Future<Double> power: Stored_powers)
                result = result + power.get();
            System.out.println("il risulato è " + result);

        } catch (ExecutionException e){
            System.out.println(e.getMessage());
        } catch (InterruptedException e){
            System.out.println(e.getMessage());
        } finally {
            Threadpool.shutdown();
        }
        
    }
}