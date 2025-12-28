/*
* Il log file di un web server contiene un insieme di linee, con il seguente formato:
* 150.108.64.57 - - [15/Feb/2001:09:40:58 -0500] "GET / HTTP 1.0" 200 2511
*
* in cui:
* 150.108.64.57 indica l'host remoto, in genere secondo la dotted quad form
* [data]
* "HTTP request" è il tipo di richiesta http
* status
* bytes sent
* eventuale tipo del client "Mozilla/4.0......."
*
* Scrivere un'applicazione Weblog che prende in input il nome del log file
* e ne stampa ogni linea, in cui ogni indirizzo IP è sostituito con l'hostname.
* Sviluppare due versioni del programma, la prima single-threaded,
* la seconda invece utilizza un thread pool,
* in cui il task assegnato ad ogni thread riguarda la traduzione di un insieme di linee del file.
* Confrontare i tempi delle due versioni.
*/

import java.io.File;

public class MainClass {
    public static void main(String[] args){

        if (args.length==0)
            System.err.println("Usage: MainClass logfile\n"
                                + "\tlogfile \t percorso al logfile\n"
                                + "\nExample: MainClass /User/Desktop/Cartella1/logfile");

        //setto parametri da inserire per creare una readtask
        String logfile = args[0];
        File f = new File(logfile);
        int len = (int) f.length();
        String[] s = new String[0];

        //creo single tread che gestisce istanza di readtask
        Thread thread = new Thread(new ReadTask(logfile, s , 0, len));

        long time1 = System.currentTimeMillis();
        //attivo single thread
        thread.start();
        try {
            //chiudo single thread
            thread.join();
        } catch (InterruptedException e) { }
        long time2 = System.currentTimeMillis();


        ThreadPool CurrentThreadpool = new ThreadPool();

        long time3 = System.currentTimeMillis();
        //attivo threadPool
        CurrentThreadpool.execute(logfile, len);
        //chiudo threadPool
        CurrentThreadpool.shoutDown();
        long time4 = System.currentTimeMillis();

        System.err.println("\t\tdal single thread tempo trascorso è " + (time2 - time1) + "millisec");
        System.err.println("\t\tdal pool tempo trascorso è " + (time4 - time3) + "millisec");

    }
}
