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

public class MainClasss {
    public static void main(String[] args) {
        ArrayList<Future<Double>> results = new ArrayList<Future<Double>>();
        
    }
}
