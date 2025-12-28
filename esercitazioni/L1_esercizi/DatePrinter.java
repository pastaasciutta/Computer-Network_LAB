package lab_reti.L1_esercizi;

import java.util.Calendar;

public class DatePrinter {
    public static void main(String args[]){
        while(true){
            Calendar calendar = Calendar.getInstance();
            System.out.printf(calendar.getTime() + " %s\n", Thread.currentThread().getName());

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.printf("Sleep interrupted\n");
            }

            System.out.printf("Excecuted successfuly\n");
        }
        //System.out.printf(" %s\n", Thread.currentThread().getName());
    }
}
