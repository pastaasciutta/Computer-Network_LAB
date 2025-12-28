package lab_reti.L1_assignments;
  
import java.lang.Math;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.Scanner;

public class MainClass{
    public static void main(String[] args){
        try {
            //per leggere roba da input devo dichiarare un oggetto (in questo caso sc) di tipo scanner 
            Scanner sc = new Scanner(System.in).useLocale(Locale.getDefault());
            //prendo in scanf attraverso sc l'incertezza assoluta (accuratezza) e il tempo
            System.out.println("inserisci l'accuratezza:");
            Double x = sc.nextDouble();
            System.out.println("inserisci il tempo limite:");
            long y = sc.nextLong();
            //dichiaro una classe di tipo PiGreco nome pg
            PiGreco pg = new PiGreco(x);

            //dichiaro thread per la struttura pg e lo faccio partire
            Thread thread = new Thread(pg);
            thread.start();
            /*faccio dormire il thread del main per il tempo massimo che concedo al thread di runnable prima di terminarlo
            Thread.sleep(y); sbagliato perche usando sllep e join dorme piu del tempo necessaario*/

            //faccio "terminare" il thread di pg in maniera sicura aspettandolo per y millisec
            thread.join(y);
            //is alive è un bool che ritorna true se il thread è ancora attivo 
            if (thread.isAlive()){
                //se è ancora attvo lo interrompo (esendo passati y millisec)
                thread.interrupt();
                System.out.println("il calcolo del pi è stato interrotto, avendo superato il tempo limite");
            }
            //chiudo sc
            sc.close();
        } catch (InputMismatchException | InterruptedException e){
            /*InputMismatchException mi controlla che l'input sia del tipo giusto
              | è come uno xor
              InterruptedException gestisce l'eccezione/i che puo darmi sleeep o join*/
            
            //e.getMessage stampa l'errore specifico per cui sono nel catch (InputMismatchException o InterruptedException)
            System.out.println(e.getMessage());
        }
    }
}

class PiGreco implements Runnable{

    private double accuracy;
    //costruttore
    public PiGreco (double accuracy){
        //inizializzo
        this.accuracy = accuracy;
    }
    /* oppure posso scrivere
    public static set-NOME-(int qualcosa){
        this.qualcosa=qualcosa;
    }*/
    //calcolo pi
    public void run() {
        double pi = 0;
        //Math.abs serve per mettere il valore assoluto della roba che ho fra parentesi 
        for(double i = 1; accuracy < Math.abs(pi - Math.PI); i = i+4){
            //formula di Leibiniz
            pi = pi + 4*(1/i -1/(i+2));
            /* se usassi >
            pi = pi + 4/i;
            if(i > 0)
                i = (i+2)*(-1);
            else
                i = (i-2)*(-1);
            non funzionerebbe, y porché?*/
        }

        //arrotondo il pi fino all'ultima cifra significativa dettata dall'incertezza assoluta (accuracy)
        String spi = String.format("%,f%n", accuracy); //System.out.println(spi); stampa con la virgola
        //counter mi serve per sapere in che posizione dopo la virgola si trova l'1 in accuracy
        int counter = 0;

        for(int i=0; spi.charAt(i) != '1' && i<spi.length(); i++)
        {
            //inizio a contare dalla virgola perché uscirò prima di contare l'ultima posizione (quella in cui si trova l'1)
            if ( counter>0 || spi.charAt(i) == ',' )
                counter++;
        }
        //moltiplico per 10^counter perche Math.round ritorna il valore del numero arrotondato fino all'ultima cifra intera
        pi = pi * Math.pow(10, counter);
        pi = Math.round(pi);
        //divido per lo stesso numero per riavere pi
        pi = pi/Math.pow(10, counter);

        System.out.println("pi: " + pi);
    }
}