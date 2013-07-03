package baseline;

import java.util.Set;

public class DictionaryFeature implements Feature{

    private String name;
    private Set<String> dictionary;
    
    public DictionaryFeature(Set<String> dictionary) {
        this.dictionary = dictionary;
        this.name = "DICTCNT";
    }
    
    public DictionaryFeature(Set<String> dictionary, String name) {
        this.dictionary = dictionary;
        this.name = name+"_DICTCNT";
    }
    
    public String evaluateFeature(String token) {
        boolean inDictionary = dictionary.contains(token);
        
        return inDictionary? "1" : "0";
    }

    @Override
    public String getName() {
        return name;
    }

    
}
