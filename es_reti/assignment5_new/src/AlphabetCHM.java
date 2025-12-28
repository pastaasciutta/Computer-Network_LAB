import java.util.concurrent.ConcurrentHashMap;

public class AlphabetCHM {

    private ConcurrentHashMap<Character, Integer> map;

    public AlphabetCHM(){
        this.map = new ConcurrentHashMap<Character, Integer>();
    }

    public void initialize(){
        //initializing my map
        for(char letter = 'a'; letter <= 'z'; letter++)
            map.put(letter, 0);
    }

    public synchronized void Add( char k, int localValue){
        int oldValue = map.get(k);
        map.replace(k, oldValue, oldValue + localValue);
    }

    public int getValue( char k ){ return map.get(k); }
}
