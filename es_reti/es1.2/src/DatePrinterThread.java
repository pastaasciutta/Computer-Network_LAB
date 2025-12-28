import java.util.Calendar;
public class DatePrinterThread extends java.lang.Thread {
    public static void main (String[] args){
        DatePrinterThread DatePrinter = new DatePrinterThread();
        //executing thread
        DatePrinter.start();
        //printing threads name
        System.out.println("from main " + Thread.currentThread().getName());
    }

    @Override
    public void run(){
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
