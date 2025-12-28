import java.io.File;

public class Producer implements Runnable{

    private syncLinkedList list;
    private final File startDirectory;

    public Producer(syncLinkedList list, File startDirectory){
        this.list = list;
        this.startDirectory = startDirectory;
    }

    public void recVisit(File directory){
        if (directory != null){
            list.Add(directory.toString());

            File[] filesInCurrentDirectory = directory.listFiles();

            for (File file: filesInCurrentDirectory){
                if (file.isDirectory()){
                    recVisit(file);
                }
            }
        }
    }

    @Override
    public void run(){

        recVisit(startDirectory);

        list.setDone();
    }
}
