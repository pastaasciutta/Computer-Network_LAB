public class PrimeCount implements Runnable{

    private static int limit = 10000000;
    private int primeCount;
    private boolean[] isPrime;

    public PrimeCount(){
        this.primeCount = 0;
        this.isPrime = new boolean[limit+1];
        for (int p=0; p<limit; p++) {
            isPrime[p] = true;
        }
    }

    @Override
    public void run() {

        for (int p = 2; p * p <= limit; p++) {
            if (isPrime[p]) {
                for (int i = p * p; i <= limit; i = i+p) {
                    isPrime[i] = false;
                }
            }
        }

        for (boolean e: isPrime) {
            if(e)
                primeCount++;
        }
        primeCount--; //taking 0 off

        System.out.println("The number of prime numberbers between 1 and " + limit + " is: "+ primeCount);
    }
}
