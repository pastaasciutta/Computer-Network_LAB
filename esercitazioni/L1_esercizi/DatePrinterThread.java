package lab_reti.L1_esercizi;

import java.util.Calendar;

public class DatePrinterThread extends java.lang.Thread /*posso scrivere solo thread*/{

    public void run(){
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
    }
    public static void main(String args[]){
        DatePrinterThread dpt = new DatePrinterThread();
        dpt.start();
        System.out.printf(" %s\n", Thread.currentThread().getName());
    }
}
