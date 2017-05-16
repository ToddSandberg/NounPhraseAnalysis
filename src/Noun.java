import java.util.ArrayList;
import java.util.HashMap;

public class Noun {
    public int count=1;
    public String word = "";
    public HashMap<String,Integer> adjectives = new HashMap<String,Integer>();
    public ArrayList<Determiner> determiners = new ArrayList<Determiner>();
    public Noun(String s){
        word = s;
    }
    @Override
    public boolean equals(Object o){
        if(word.equals(((Noun)o).word)){
            return true;
        }
        else
            return false;
    }
    public double getPercentage(){
       double topPercentage = 0;
       double totaladjectives=0;
       for(int i=0;i<adjectives.size();i++){
           totaladjectives += adjectives.get(i);
       }
       return topPercentage;
    }
    public String toString(){
        return "Noun = " + word+"; occurrences = " + count + "\n" + "Different adjectives: "+adjectives.size();
    }
}
