import java.io.File;

public class Consumer implements Runnable{

    int mat;
    syncLinkedList list;

    public Consumer(int mat, syncLinkedList list){
        this.mat = mat;
        this.list = list;
    }

    @Override
    public void run() {
        //preleva testa syncLinkedLIst
        String currentDirectory = list.getHead();

        String[] filesInCurrentDirectory = (new File(currentDirectory)).list();

            if (filesInCurrentDirectory == null)
                System.exit(-1);

            String fileName;
            for (String i : filesInCurrentDirectory)
                System.out.println("consumer n " + mat + " file: " + i);
    }
}
