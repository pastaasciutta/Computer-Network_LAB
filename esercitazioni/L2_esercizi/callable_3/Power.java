//mi serve per impementare l'interfaccia callable
import java.util.concurrent.Callable;
import java.io.IOException;

public class Power implements Callable<Double>{
    private double num;
    private int exp;
    public Power(double num, int exp){
        this.num = num;
        this.exp = exp;
    }

    @Override
    public Double call() throws Exception {
        Double c=0;
        try {
            //id di un thead gli viene asssociato dalla nascita è la targa del thread
            System.out.printf("Esecuzione "+ num +"^"+ exp +" in %s\n", Thread.currentThread().getId()); 
            c = Math.pow(num, exp);
        } catch (ArithmeticException | IOException e) {
            /*ArithmeticException utile per le eccezioni che mi da math.pow
              IOException utile per le eccezioni che mi da il print*/
            System.out.println(e.getMessage());
        }
        return c;
    }
}