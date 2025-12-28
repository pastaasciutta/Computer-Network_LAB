import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class ReadJSON {

    public static void main(String[] args) {

        String print = null;

        // lettura file JSON
        try{
            print = Creator.coding();
        }catch(IOException e) {
            System.out.println(e.getMessage());
        }

        System.out.println(print);
    }

    public static class Creator{

        private static final AtomicIntegerArray Causali = new AtomicIntegerArray(5);

        public static String coding() throws IOException {

            FileChannel fileChannel = FileChannel.open( Paths.get("Acc.json"), StandardOpenOption.READ );
            StringBuilder sb = new StringBuilder();
            //creazione di un byte buffer
            ByteBuffer buffer = ByteBuffer.allocate((int)fileChannel.size());
            //lettura dal canale al buffer
            fileChannel.read(buffer); // Read the whole file into the buffer

            //sposto il puntatore dalla coda di buffer alla testa
            buffer.flip();

            while(buffer.hasRemaining()){
                //Leggo da buffer a stringa
                sb.append((char)buffer.get());
            }
            // parso con Gson attraverso la stringa
            Gson gson = new Gson();
            ArrayList<Account> accounts = gson.fromJson(sb.toString(), new TypeToken<ArrayList<Account>>(){}.getType());
            ExecutorService exec = Executors.newCachedThreadPool();

            for (Account account: accounts){
                exec.execute(new User(account, Causali));
            }

            // stringa da stampare
            ArrayList<String> typeCausali = Tipi();
            StringBuilder result = new StringBuilder();
            for(int j = 0; j < 5; j++){
                result.append("\n")
                        .append(typeCausali.get(j))
                        .append(":\t")
                        .append(Causali.get(j));
            }
            exec.shutdown();
            return result.toString();
        }

        private static ArrayList<String> Tipi(){
            ArrayList<String> typeCausali = new ArrayList<>();
            typeCausali.add("Bonifico");
            typeCausali.add("Accredito");
            typeCausali.add("Bollettino");
            typeCausali.add("F24");
            typeCausali.add("PagoBancomat");
            return typeCausali;
        }
    }

    public static class User implements Runnable{

        private final Account account;
        private final AtomicIntegerArray array;

        public User(Account account, AtomicIntegerArray array) {
            this.account = account;
            this.array = array;
        }

        public void run() {

            for(Transazioni payment: account.getPayments()){
                array.getAndIncrement(payment.getCausale());
            }
        }
    }
}
