public class DropboxMonitor extends Dropbox{

    @Override
    public synchronized int take(boolean e){
        String s = e ? "Pari" : "Dispari";

        while (!full || e == (num % 2 != 0)) { //num non è quello cercato
            System.out.println("Attendi per: " + s);
            //aggiungo wait
            try {
                this.wait();
            } catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }

        try {
            Thread.sleep((long) (Math.random()*1000));
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        System.out.println(s + " <-> " + num);
        full = false;
        //aggiungo notify
        this.notifyAll();
        return num;
    }

    @Override
    public synchronized void put(int n){
        while (full) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Producer ha inserito " + n);
        num = n;
        full = true;
        notify();
    }
}
