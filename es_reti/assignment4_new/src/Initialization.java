import java.io.File;

public class Initialization implements Runnable{

    private DirectoryList list;
    private final String[] args;

    public Initialization (DirectoryList list, String[] args){
        this.list = list;
        this.args = args;
    }

    @Override
    public void run() {
        //inizializzo directoryList
        for (String path : args) {
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                list.Add(path);
                recVisit(file);
            }
        }
        list.setDone();
    }

    public void recVisit(File directory){
        //adding current directory to list
        list.Add(directory.getAbsolutePath());

        File[] filesInCurrentDirectory = directory.listFiles();

        if (filesInCurrentDirectory != null){
            for (File file: filesInCurrentDirectory){
                //visiting only directories
                if (file.isDirectory())
                    recVisit(file);
            }
        }
    }
}
