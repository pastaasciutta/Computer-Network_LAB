import java.util.InputMismatchException;

public class PiGreco implements Runnable{

    double accuracy;
    //constructor
    public PiGreco (double accuracy){
        this.accuracy = accuracy;
    }
    /* or
       public static set-NAME-(int smth){
       this.smth = smth;
    }
    */

    //calculating pi
    @Override
    public void run() {

        double pi = 0;
        //calculating pi with Leibniz's formula
        //stop when reached accuracy value
        //Math.abs for absolute value
        for (double i = 1; accuracy < Math.abs(pi - Math.PI); i = i+4) {
            pi = pi + 4 *( 1/i - 1/(i+2) );
        }

        //for rounding pi on its last significant number (accuracy)
        //convert double to string to find 1's position
        String stringPi = String.valueOf(accuracy);
        //take only the last two char in stringPi
        String lastTwo = substring(stringPi, stringPi.length()-2);

        //position takes trace of 1's position in accuracy
        int position = 0;

        //accuracy can be converted to string in
        if(lastTwo.equals("01"))
            //1 -> 0.001 type
            position = stringPi.length()-2;
        else{
            //or 2 1.0E-3 type
            position = Integer.parseInt(lastTwo);
        }

        // multiply pi with 10^counter
        pi = pi * Math.pow(10, position);

        // Math.round returns the rounded INTEGER number
        pi = Math.round(pi);

        // divide pi with 10^counter
        pi = pi/Math.pow(10, position);

        System.out.println("pi: " + pi);
    }

    String substring(String string, int beginIndex){
        StringBuilder newString = new StringBuilder(string);
        if(string.charAt(beginIndex) == '-')
            beginIndex++;
        return newString.substring(beginIndex);
    }
}
