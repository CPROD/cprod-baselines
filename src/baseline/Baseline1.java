package baseline;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.arabidopsis.ahocorasick.AhoCorasick;
import org.arabidopsis.ahocorasick.SearchResult;

public class Baseline1 extends BaselineModel{

    Map<String, String[]> term2product;
    String productDictionaryFile;
    
    public Baseline1() {
        term2product = new HashMap<String, String[]>();
    }
    
    public Baseline1(String productDictionaryFile) {
        this();
        this.productDictionaryFile = productDictionaryFile;
    }
    
    Map<String, ArrayList<Annotation>> test(TextCorpus textitem) {
        AhoCorasick ahoCorasick = new AhoCorasick();
        
        //Add the search terms to the AhoCorasick algorithm
        for(Map.Entry<String, String[]> term : term2product.entrySet()) {
            String keyword = " "+term.getKey()+" ";
            ahoCorasick.add(keyword.getBytes(), term.getKey());
        }
        
        ahoCorasick.prepare();
        Map<String, ArrayList<Annotation>> annotationResult = new HashMap<String, ArrayList<Annotation>>();
        
        for(Map.Entry<String, String[]> text : textitem.entrySet()) {
            String[] tokens = new String[text.getValue().length];
            for(int j=0; j<tokens.length; j++) {
                tokens[j] = normalizeToken(text.getValue()[j]);
            }
            String concatenatedTokens = "";
            for(String token : tokens) {
                concatenatedTokens += " " + token;
            }
            
            concatenatedTokens = " "+concatenatedTokens+" ";
            
            Iterator<SearchResult> i = ahoCorasick.search(concatenatedTokens.getBytes());
            Map<Integer, String> mentions = new HashMap<Integer, String>();
            while(i.hasNext()) {
                SearchResult result = i.next();
                String product = (String) result.getOutputs().toArray()[0];
                int firstBytePos = result.getLastIndex() - (" "+product+" ").getBytes().length; //This first bytepos should contain a space
                mentions.put(firstBytePos, product);
            }
            
            ArrayList<Annotation> annotations = new ArrayList<Annotation>();
            for(int j=0, bytecount=1; j<tokens.length;){
                int tokenlength = (" "+tokens[j]).getBytes().length;
                if(mentions.containsKey(bytecount)) {
                    String product = mentions.get(bytecount);
                    int productlength = product.split(" ").length;
                    annotations.add(new Annotation(text.getKey(), j, j+productlength-1,term2product.get(product)));
                    for(int k=j-2; k<=j+productlength+1; k++)
                        if(k>=0 && k<text.getValue().length)
                            System.out.print(text.getValue()[k]+" ");
                    System.out.println(text.getKey()+" \'"+product+"\'"); //DEBUG
                    
                    j += productlength;
                    bytecount += (" "+product).getBytes().length;
                }
                else {
                    j++;
                    bytecount += tokenlength;
                }
            }
            if(!annotations.isEmpty()) annotationResult.put(text.getKey(), annotations);
        }
        
        return annotationResult;
    }

    /*
     * Goes through the annotated training text and builds a reverse hash map from terms to product ids.
     * @see BaselineModel#train(TextItem, java.util.Map)
     */
    void train(TextCorpus annotatedText, Map<String, ArrayList<Annotation>> annotations) throws IOException {
        for(Map.Entry<String, ArrayList<Annotation>> annotationsList : annotations.entrySet()) {
            String id = annotationsList.getKey();
            String[] text = annotatedText.get(id);
            for(Annotation annotation : annotationsList.getValue()){
                String term = text[annotation.startToken]; 
                for(int i=annotation.startToken+1; i<=annotation.endToken; i++){
                    term += " "+text[i];
                }
                String[] termTokens = Arrays.copyOfRange(text, annotation.startToken, annotation.endToken);
                term = normalizeToken(term);
                
                //If the term is not yet in the map, or we have a longer list of products than in the map, 
                // put this term+products in the map
                if(term.length()> 2 && (!term2product.containsKey(term) || term2product.get(term).length < annotation.productIDs.length)) {
                    term2product.put(term, annotation.productIDs);
                }
            }
        }
        if(productDictionaryFile!=null)
            addProductsFromDictionary(productDictionaryFile);
    }

    void addProductsFromDictionary(String additionalDictionaryFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(additionalDictionaryFile));
        String line = reader.readLine();
        while(line!=null && !line.equals("")){
            String[] lineArray = line.split("\t");
            if(!term2product.containsKey(lineArray[0])) {
                String[] products = {};
                if(lineArray.length>1)
                    products = lineArray[1].split(" ");
                term2product.put(lineArray[0], products);
            }
            line = reader.readLine();
        }
        reader.close();
    }
    
}
