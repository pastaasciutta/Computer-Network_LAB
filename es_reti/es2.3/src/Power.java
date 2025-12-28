import java.util.concurrent.Callable;

/*The Callable interface is similar to Runnable, in that both are designed
  for classes whose instances are potentially executed by another thread.
  A Runnable, however, does not return a result and cannot throw a checked exception.*/
public class Power implements Callable<Double> {

    private double base;
    private int exp;
    public Power(double base, int exp){
        this.base= base;
        this.exp= exp;
    }

    @Override
    public Double call() throws Exception {
        System.out.format("Esecuzione %f^%d in thread %d%n", this.base, this.exp, Thread.currentThread().getId());
        return Math.pow(this.base, this.exp);
    }
}
