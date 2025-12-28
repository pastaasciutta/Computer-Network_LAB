import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/*
* Sviluppare un'applicazione client server in cui il server gestisce le partite giocate in un semplice gioco,
* “Dungeon adventures” basato su una semplice interfaccia testuale
*
* ad ogni giocatore viene assegnato, ad inizio del gioco:
* un livello X di salute
* una quantità Y di una pozione, (X e Y generati casualmente)
*
* ogni giocatore combatte con un mostro diverso.
* Anche al mostro assegnato a un giocatore viene associato, all'inizio del gioco:
* un livello Z di salute generato casualmente
*
* il gioco si svolge in round, ad ogni round un giocatore può:
* combattere con il mostro:
*   il combattimento si conclude decrementando il livello di salute del mostro e del giocatore.
*       Se LG è il livello di salute attuale del giocatore e MG quello del mostro,
*       tale livello viene decrementato di un valore casuale X, con 0<=X<=LG.
*       Analogamente, per il mostro si genera un valore casuale K, con 0<=K<=MG.
* bere una parte della pozione:
*   la salute del giocatore viene incrementata di un valore proporzionale alla quantità di pozione bevuta,
*   che è un valore generato casualmente.
* uscire dal gioco:
*   in questo caso la partita viene considerata persa per il giocatore.
*
* il combattimento si conclude quando il giocatore o il mostro o entrambi hanno un valore di salute pari a 0.
* Se il giocatore ha vinto o pareggiato, può chiedere di giocare nuovamente, se invece ha perso deve uscire dal gioco.
*
* Sviluppare una applicazione client server che implementi Dungeon adventures:
*
* il server riceve richieste di gioco da parte dei cliente e gestisce ogni connessione in un diverso thread
  *     ogni thread riceve comandi dal client li esegue.
  *     Nel caso del comando “combattere”, simula il comportamento del mostro assegnato al client.
  *  dopo aver eseguito ogni comando ne comunica al client l'esito.
  * comunica al client l'eventuale terminazione del del gioco, insieme con l'esito.
*
* il client si connette con il server chiede iterativamente all'utente il comando da eseguire e lo invia al server.
  * I comandi sono i seguenti 1:combatti, 2: bevi pozione, 3: esci del gioco
  *     attende un messaggio che segnala l'esito del comando nel caso di gioco concluso vittoriosamente,
  *     chiede all'utente se intende continuare a giocare e lo comunica al server
* */
 class ServerMainClass {
     static final int port = 8888;
     static final int timeOut = 300000;
     static boolean expiredTime = false;

    public static void main(String[] args) throws IOException {
        //passive socket in ascolto V client che si collega
        try(ServerSocket listener = new ServerSocket(port, Integer.MAX_VALUE, InetAddress.getLocalHost())){

            listener.setSoTimeout(timeOut);
            System.out.println("\t Dungeon adventures server is running...");
            ServerThreadPool pool = new ServerThreadPool();
            //se passa n unità di tempo senza ricevere richieste -> server chiude la passive socket
            while(!expiredTime){
                try {
                    //E client i fa la richiesta -> server crea socket attiva per poi attivare la sessione di gioco
                    Socket socket = listener.accept();
                    pool.execute(new ServerDungeonAdventures(socket));
                } catch (SocketException e){
                    expiredTime = true;
                }
            }
            pool.shoutDown();
        }
    }
}
