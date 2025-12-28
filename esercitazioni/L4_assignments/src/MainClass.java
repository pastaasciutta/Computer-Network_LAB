import java.util.InputMismatchException;
import java.util.Locale;
import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in).useLocale(Locale.getDefault());
        try{
            System.out.println("inserisci il numero di professori:");
            int prof = scanner.nextInt();

            System.out.println("inserisci il numero di tesisti:");
            int thesists = scanner.nextInt();

            System.out.println("inserisci il numero di studenti:");
            int stud = scanner.nextInt();

            int num = prof + thesists + stud;
            Tutor tutor = new Tutor(num);
            //chiamo il metodo queue di Tutor che attiva n threads
            tutor.queue(num, prof, thesists, stud);
            //chiudo l'aula una volta che tutti i threads hanno finito le task
            tutor.closeComputerRoom();
        } catch (InputMismatchException e){
            System.out.println(e.getMessage());
        } finally {
            scanner.close();
        }
    }
}