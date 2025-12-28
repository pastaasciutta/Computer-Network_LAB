/*Simulare il flusso di clienti in un ufficio postale che ha 4 sportelli.
Nell'ufficio esiste:

- un'ampia sala d'attesa in cui ogni persona può entrare liberamente.
  Quando entra, ogni persona prende il numero dalla numeratrice e aspetta il proprio turno in questa sala.

- una seconda sala, meno ampia, posta davanti agli sportelli,
  in cui possono essere presenti al massimo k persone (oltre alle persone servite agli sportelli)

- Una persona si mette quindi prima in coda nella prima sala, poi passa nella seconda sala.

- Ogni persona impiega un tempo differente per la propria operazione allo sportello.
  Una volta terminata l'operazione, la persona esce dall'ufficio

Scrivere un programma in cui:

- l'ufficio viene modellato come una classe JAVA, in cui viene attivato
  un ThreadPool di dimensione uguale al numero degli sportelli

- la coda delle persone presenti nella sala d'attesa è gestita esplicitamente dal programma

- la seconda coda (davanti agli sportelli) è quella gestita implicitamente dal ThreadPool

- ogni persona viene modellata come un task, un task che deve
  essere assegnato ad uno dei thread associati agli sportelli

- si preveda di far entrare tutte le persone nell'ufficio postale, all'inizio del programma*/

import java.util.Locale;
import java.util.Scanner;

public class MainClass {
  public static void main(String[] args) {
    Scanner scan = new Scanner(System.in).useLocale(Locale.getDefault());

    System.out.println("inserisci il numero di persone nell'ufficio");
    int clienti = scan.nextInt();

    System.out.println("inserisci la capienza massima nella seconda sala");
    int k = scan.nextInt();

    UfficioPostale UniPoste = new UfficioPostale(clienti, k);
    UniPoste.sala1(clienti);

    UniPoste.chiudi_ufficio();
    scan.close();
  }
}