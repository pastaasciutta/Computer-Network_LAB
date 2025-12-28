import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class WriteJSON {

    private final static int nAccounts = 100;
    private final static String[] nomi = { "anna", "anna1", "anna2", "anna3", "anna4",
                                           "anna5", "anna6", "anna7", "anna8", "anna9" };
    private final static String[] cognomi = { "nana", "nana", "nana", "nana", "nana",
                                              "nana", "nana", "nana", "nana", "nana"};

    public static void main(String[] args) {

        WritableByteChannel file = null;
        try{
            //apro file o ne creo uno se non esiste
            file = FileChannel.open( Paths.get("Acc.json"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        }catch (IOException e){
            System.out.println(e.getMessage());
        }

        //alloco 500 MB
        ByteBuffer buffer = ByteBuffer.allocate(500 * 1024);

        // array di account
        ArrayList<Account> accounts = new ArrayList<>(nAccounts);

        //creol un nuovo account
        for(int i = 0; i < nAccounts; i++){
            accounts.add(newRandAcc());
        }
        //al solito da scritura a lettura
        buffer.flip();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //account diventano una stringa
        String json = gson.toJson(accounts);
        //butto il contenuto di json nel buffer (ste variabili rimarranno l'una vincolata dall'altra)
        buffer = ByteBuffer.wrap(json.getBytes());

        try{
            //scrivo nel buffer
            file.write(buffer);
            System.out.println("file JSON creato con successo");
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
        finally {
            buffer.clear();
        }
    }

    private static Account newRandAcc(){

        final int nTransactions = 1000;
        //raccolta di nomi e cognomi
        String fullname = (nomi[(int) (Math.random() * nomi.length)] + " " + cognomi[(int) (Math.random() * cognomi.length)]);
        int nTrans = (int) (Math.random() * nTransactions);

        Account acc = new Account(fullname, new ArrayList<>(nTrans));

        for (int i = 0; i < nTrans; i++){
            acc.addPayment(new Transazioni(randDate(), ThreadLocalRandom.current().nextInt(5)));
        }
        return acc;
    }

    private static String randDate(){
        // creo data
        int day = (int) (Math.random() * 31);
        int month = (int) (Math.random() * 12);
        int year = (int) (Math.random() * 2021);
        return day + "/" + month + "/" + year;
    }
}

