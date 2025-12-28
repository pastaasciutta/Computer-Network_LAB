import java.util.InputMismatchException;
import java.util.Locale;
import java.util.Scanner;

public class MainClass {

    public static void main(String[] args){
        try {
            Scanner scanner = new Scanner(System.in).useLocale(Locale.getDefault());

            //take from scanner incertezza assoluta (accuracy) and time
            System.out.println("inserisci l'accuratezza: ");
            double x = scanner.nextDouble();

            System.out.println("inserisci il tempo limite in millisecondi: ");
            long y = scanner.nextLong();

            scanner.close();

            PiGreco piGreco = new PiGreco(x);
            //thread for PiGreco
            Thread thread = new Thread(piGreco);
            thread.start();

            //give Pigreco's thread y millisec and then interrupt it (safe procedure with join)
            thread.join(y);

            //is alive is a bool
            if (thread.isAlive()){
                System.out.println("il calcolo del pi è stato interrotto, avendo superato il tempo limite");
                //if yes interrupt it
                thread.interrupt();
            }
        } catch (InputMismatchException | InterruptedException e){
            /*InputMismatchException checks if the input matches his type
              | is a xor
              InterruptedException handles exceptions given by join or sleep*/

            //e.getMessage prints the specific error
            System.out.println(e.getMessage());
        }

    }
}
