package baseline;

import java.util.HashSet;

public class Dictionary extends HashSet<String>{
    private String name;
    
    public Dictionary(String name) {
        super();
        this.name=name;
    }
    
    public String getName() {
        return name;
    }
}
