import java.util.ArrayList;
// Lab modella un laboratorio in cui sono presenti n computers
public class Lab {

    /*I computers del laboratorio sono rappresentati dall'arraylist di Bool
      (true computer libero, false occupato)*/
    private ArrayList<Boolean> computers;
    // n = numero di computers
    private final int n;

    public Lab (int n){
        this.n = n;
        this.computers = new ArrayList<>(n);
        for (int i=0; i<n; i++){
            computers.add(Boolean.TRUE);
        }
    }

    public int getN(){ return n;}

    //trova il primo computer libero (per lo studente)
    public int getAvailableComputer(){
        int j=0;
        for(Boolean i: computers){
            if(i)
                return j;
            j++;
        }
        return -1;
    }
    //controlla se il computer specifico è libero (per il tesista)
    public boolean isAvailable(int id){ return computers.get(id);}
    //occupa computer
    public void occupyComputer(int id){ computers.set(id, false);}
    //libera computer
    public void releaseComputer(int id){ computers.set(id, true);}

    //controlla se l'aula è libera (per il professore)
    public boolean isFree(){
        for (Boolean i: computers){
            if(!i)
                return false;
        }
        return true;
    }
    //occupa l'intero laboratorio
    public void occupyAll(){
        for(Boolean i: computers)
            i = false;
    }
    //libera l'intero laboratorio
    public void releaseAll(){
        for(Boolean i: computers)
            i = true;
    }
}
