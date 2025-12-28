/**
 * Il laboratorio di Informatica del Polo Marzotto è utilizzato da tre tipi di utenti, studenti, tesisti e professori
 * ed ogni utente deve fare una richiesta al tutor per accedere al laboratorio.
 * I computers del laboratorio sono numerati da 1 a 20.
 * Le richieste di accesso sono diverse a seconda del tipo dell'utente:
 *
 * - i professori accedono in modo esclusivo a tutto il laboratorio, poichè hanno necessità di utilizzare
 *   tutti i computers per effettuare prove in rete.
 * - i tesisti richiedono l'uso esclusivo di un solo computer, identificato dall'indice i,
 *   poichè su quel computer è installato un particolare software necessario per lo sviluppo della tesi.
 * - gli studenti richiedono l'uso esclusivo di un qualsiasi computer.
 *
 * I professori hanno priorità su tutti nell'accesso al laboratorio, i tesisti hanno priorità sugli studenti.
 * Nessuno può essere interrotto mentre sta usando un computer.
 * Scrivere un programma JAVA che simuli il comportamento degli utenti e del tutor.
 * Il programma riceve in ingresso il numero di studenti, tesisti e professori che utilizzano
 * il laboratorio ed attiva un thread per ogni utente.
 * Ogni utente accede k volte al laboratorio, con k generato casualmente.
 * Simulare l'intervallo di tempo che intercorre tra un accesso ed il successivo e l'intervallo di permanenza
 * in laboratorio mediante il metodo sleep.
 * Il tutor deve coordinare gli accessi al laboratorio. Il programma deve terminare quando tutti
 * gli utenti hanno completato i loro accessi al laboratorio.
 *
 *
 * (con l'utilizzo dei monitor)
 *
 * @author Marta Menna
 * @version 1.0
 */

import java.util.Random;
public class MainClass {
    private static final int N_COMPUTERS_MARZOTTO = 20;

    public static void main(String[] args) {
        if(args.length != 3) {
            System.err.println("Usage: MainClass numStudenti numTesisti numProfessori \n"
                    + "\tnumstudenti \t numero di studenti che accedono al laboratorio \n "
                    + "\tnumTesisti \t numero di tisti che accedono al laboratorio\n"
                    + "\tnumProfessori \tnumero di professori che accedono al laboratorio\n"
                    + "\n\nExample: MainClass 10 5 2.");
            System.exit(1);
        }

        /** @param ns numero studenti
        *
        * */
        int ns = 0;  // numero di studenti
        int nt = 0;  // numero di tesisti
        int np = 0;  // numero di professori

        ns = Integer.parseInt(args[0]);
        nt = Integer.parseInt(args[1]);
        np = Integer.parseInt(args[2]);

        Lab labMarzotto = new Lab(N_COMPUTERS_MARZOTTO);

        Tutor t = new Tutor(labMarzotto);

        // crea e avvia gli studenti
        for(int i=0; i<ns;i++)
            new Thread(new Student(t, i, 2)).start();

        // crea e avvia i tesisti
        for(int i=0; i<nt;i++)
            new Thread(new Tesist(t, i,  3)).start();

        // crea e avvia i professori
        for(int i=0; i<np;i++)
            new Thread(new Professor(t, i, 3)).start();
    }

}
