import java.util.Calendar;

public class DatePrinterRunnable implements Runnable{
    public static void main(String[] args) {
        //dichiara oggetto di tipo DatePrinterRunnable
        DatePrinterRunnable dpr = new DatePrinterRunnable();
        //dichiara oggetto di tipo thread che prende come paramatro del costruttore dpr
        Thread T1 = new Thread(dpr);
        /* oppure
        Thread dpt = new Thread(new DatePrinterRunnable());*/
        //manda in esecuzione il thread
        T1.start();
        //stampa il nome del thread corrente
        System.out.println("from main " + Thread.currentThread().getName());
    }

    @Override
    public void run() {
        while(true){
            //printing current date and time
            System.out.println(Calendar.getInstance().getTime());
            //printing current threads name
            System.out.println("from run " + Thread.currentThread().getName());
            //putting current thread in sleep for 2 seconds;
            try{
                Thread.sleep(2000);
            } catch (InterruptedException e){
                System.out.println("Sleep interrotta");
                return;
            }
        }
    }
}
