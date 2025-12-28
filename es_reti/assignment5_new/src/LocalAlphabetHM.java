import java.util.HashMap;

public class LocalAlphabetHM {

    HashMap<Character, Integer> map;
    public LocalAlphabetHM(){
        this.map = new HashMap<Character, Integer>();
        //initializing my map
        for(char letter = 'a'; letter <= 'z'; letter++)
            map.put(letter, 0);
        //System.out.println("\t" + map);
    }

    public void Add(char k){

        if (map.containsKey(k)){
            int v = map.get(k);
            //for debug
            if (!map.replace(k, v, ++v))
                System.out.println("error in LocalAlphabetHM.Add");
        }
    }

    public int getValue(char k){ return map.get(k); }
}
