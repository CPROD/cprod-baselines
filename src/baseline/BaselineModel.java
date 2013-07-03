package baseline;
import java.util.ArrayList;
import java.util.Map;


public abstract class BaselineModel {

    abstract void train(TextCorpus annotatedText, Map<String, ArrayList<Annotation>> annotations) throws Exception;
    
    abstract Map<String, ArrayList<Annotation>> test(TextCorpus textitem) throws Exception;
    
    
    public static String normalizeToken(String token) {
        token = token.toLowerCase();
        token = token.replaceAll("[^a-zA-Z0-9 ]", ""); //remove special characters
        //token.replaceAll("(.)+", ".");
        token = token.replaceAll("\\s+", " "); //remove duplicate whitespaces
        token = token.trim(); //remove leading and tailing spaces
        return token;
    }
    
}
