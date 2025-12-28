import java.util.Calendar;
public class DatePrinter {
    public static void main (String[] args){
        while(true){
             System.out.printf( Calendar.getInstance().getTime() + "\n",
                     Thread.currentThread().getName() );
             try{
                 Thread.sleep(2000);
             } catch (InterruptedException e) {
                 System.out.println("Sleep interrupted");
             }
        }
        //System.out.println(Thread.currentThread().getName());
    }
}