package baseline;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class RulebaseBaseline extends BaselineModel {

    private Map<String, Set<Product>> index;
    Set<String> productWords;
    
    static Set<String> blacklistPatterns = Sets.newHashSet(new String[]{
       "ml", "mg", "mm", "cm",
       "mm", "hz", "m", "l",
       "kg", "db", "khz", "g",
       "lt", "lbs", "mhz", "bit",
       "ft", "inch",
       "am", "pm", "sec", "s",
       "b", "w", "h", "f", "v", "s", "c", "x", "p", "k",
       "ms", "rpm", "cc", "min",
       "point", "mb", "hrs", 
       "hr", "ohm", "th", "rd", "st"
    });
    
    static Set<String> blacklistWords = Sets.newHashSet(new String[]{
            "the", "over", "for", "of", "in", "on", "with" //+maybe grammatical words
    });
    
    
    public RulebaseBaseline(String indexFile) throws IOException {
        CatalogIndexer indexer = new CatalogIndexer(indexFile);
        index = indexer.getIndex();
        productWords = Sets.newHashSet();
    }
    
    @Override
    public Map<String, ArrayList<Annotation>> test(TextCorpus textitem) throws Exception {
        Map<String, ArrayList<Annotation>> annotationsResult = Maps.newHashMap();
        for(String textID : textitem.keySet()) {
            annotationsResult.put(textID, getAnnotations(textID, textitem.get(textID)));
        }
        
        return annotationsResult;
    }

    @Override
    public void train(TextCorpus annotatedText, Map<String, ArrayList<Annotation>> annotations) throws Exception {
        for(String textID : annotatedText.keySet()) {
            String[] text = annotatedText.get(textID);
            productWords.addAll(getPossibleProducts(text));
        }
        writeProducts("rule-products.txt");
        System.out.println("done.");
    }
    
    public void writeProducts(String fileName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        for(String s : productWords)
            writer.write(s+"\n");
        writer.close();
    }
    
    private ArrayList<Annotation> getAnnotations(String textID, String[] text) {
        ArrayList<Annotation> annotations = Lists.newArrayList();
        for(int i=0; i<text.length; i++) {
            Set<Product> productReferences = index.get(normalizeToken(text[i]));
            if(productReferences!=null && 
                    !productReferences.isEmpty() &&
                    productReferences.size()<=10 &&
                    productReferences.size()>= 1 &&
                    isModelWord(text, i)) {
                //try to match surrounding words
                int left = i;
                boolean withinMention = true;
                Set<Product> currentReferences = productReferences;
                while(left>=1 && withinMention) {
                    left--;
                    
                    String token = normalizeToken(text[left]);
                    
                    //Get product references for this token and see if they overlap with the mention word
                    currentReferences = index.get(token);
                    if(currentReferences==null)
                        currentReferences = Sets.newHashSet();
                    if(Sets.intersection(productReferences, currentReferences).isEmpty())
                        withinMention = false;
                    
                }
                if(left==i)
                    left = i-1;

                
                int right = i;
                withinMention = true;
                currentReferences = productReferences;
                while(right<text.length-1 && withinMention) {
                    right++;
                    
                    String token = normalizeToken(text[right]);
                    
                  //Get product references for this token and see if they overlap with the mention word
                    currentReferences = index.get(token);
                    if(currentReferences==null)
                        currentReferences = Sets.newHashSet();
                    if(Sets.intersection(productReferences, currentReferences).isEmpty())
                        withinMention = false;
                    
                }
                if(right==i)
                    right = i+1;
                
                //Add the mention
                String[] productIDs = new String[productReferences.size()];
                int k=0;
                for(Product p : productReferences) {
                    productIDs[k] = p.getId();
                    k++;
                }
                Annotation annotation = new Annotation(textID, left+1, right-1,productIDs);
                annotations.add(annotation);
            }
        }
        
        return annotations;
    }

    @VisibleForTesting
    public Set<String> getPossibleProducts(String[] text) {
        Set<String> possibleProducts = Sets.newHashSet();
        List<Annotation> annotations = getAnnotations("", text);
        for(Annotation a : annotations) {
            possibleProducts.add(StringUtils.join(Arrays.copyOfRange(text, a.startToken, a.endToken+1)," "));
        }
        return possibleProducts;
    }
    
    
    @VisibleForTesting
    public static boolean isModelWord(String[] text, int index) {
        String normalizedToken = normalizeToken(text[index]);
        //has to contain a number
        if(!normalizedToken.matches(".*[0-9].*"))
            return false;
        
        //not proceeded by 'posted by'
        //if(index>1 && normalizeToken(text[index-2]).equals("posted") && normalizeToken(text[index-1]).equals("by"))
          //  return false;
        
        //cannot be a number of length <= 4
        if(normalizedToken.length()<=4 && !normalizedToken.matches(".*[^0-9].*") )
                return false;
        
        if(matchesBlacklistPattern(normalizedToken))
            return false;
        
        return true;
    }
    
    public static boolean matchesBlacklistPattern(String token) {
        for(String regex : blacklistPatterns) {
            if(token.matches("[0-9]*"+regex+"$"))
                    return true;
            if(token.matches("[0-9]*\\.[0.9]*"+regex+"$"))
                return true;
            if(token.matches("[0-9]*-"+regex+"$"))
                return true;
        if(token.matches("[0-9]*\\.[0.9]*-"+regex+"$"))
            return true;
        }
        return false;
    }
}
