package baseline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.spelling.PossibilityIterator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.stream.JsonReader;

/*
 * Class that holds an term2product index, either built from the product catalog or read from a file.
 */
public class CatalogIndexer {

    private List<Product> products;
    private Map<String, Set<Product>> index;
    
    public CatalogIndexer() {
        products = Lists.newArrayList();
        index = Maps.newHashMap();
        
    }
    
    public CatalogIndexer(String file) throws IOException {
        this();
        readIndexFromFile(file);
    }
    
  
    //The following blacklist of patterns comes from contestant#5
    private final String[] blacklistWords = new String[] {
            "^decalgirl",
            "^\\w+ adapter",
            "^adapter",
            "^cable",
            "^\\w+ cable",
            "^\\w+ \\w+ cable",
            "^case",
            "^\\w+ case",
            "^skin",
            "^\\w+ skin",
            "^\\w+ \\w+ skin",
            "(^|\\s)ceiling mount(s?)",
            "^battery",
            "^batteries",
            "^\\w+ battery",
            "^\\w+ batteries",
            "(^|\\s)remote control(s?)",
            "^replacement battery",
            "^replacement \\w+ battery",
            "^replacement batteries",
            "^replacement \\w+ batteries",
            "replacement battery$",
            "replacement \\w+ battery$",
            "replacement batteries$",
            "replacement \\w+ batteries$",
            "^power cord(s?)",
            "power cord(s?)$",
            "^power supply",
            "power supply$",
            "(^|\\s)service manual(s?)",
            "(^|\\s)pedestal stand",
            "(\\s)case(s?)$",
            "^\\d+%",
            "(\\s)cable(s?)$",
            "^cellallure",
            "^house charger",
            "^car charger",
            "^gelaskin",
            "(\\s)skin(s?)$",
            "^bumper",
            "(\\s)batter(y|ies)$",
            "(^|\\s)silicon case",
            "phone cover(s?)$",
            "tv list(s?)$",
            "(^|\\s)pedestal",
            //used by contestant nr 3 (to exclude accessories):
            "(\\s)for(\\s)"
    };
    
    private final String[] blacklistSubstituteWords = new String[] {
            "\\(.*\\)(\\s*)$",
            "(\\s)for\\s(.*)$",
            "\\d+(\\.?)\\d*(\\s*)x(\\s*)\\d+(\\.?)\\d*(\\s*)(\"|m|cm|mah|hz|dpi|inch(es)?|hour(s)?|min(s)?|minutes(s)?)?",
            "\\d+(\\.?)\\d*(-?)(\\s*)(\"|m|cm|mah|hz|dpi|inch(es)?|hour(s)?|min(s)?|minutes(s)?)"
    };
    
    /*
     * Reads products from a catalog file and stores them in a local list of Products.
     */
    public void readProductCatalog(String file) throws IOException {
        System.out.println("Reading products...");
        JsonReader reader = new JsonReader(new FileReader(file));
        reader.beginObject();
        reader.nextName(); // == "Product"
        reader.beginObject();
        int nprod=0, nrel=0; //DEBUG
        while(reader.hasNext()) {
            Product p = readProduct(reader);
            if(p!=null && qualifies(p) && !matchesBlacklist(p)) {
                products.add(p);
                nrel++;
            }
            nprod++;
        }
        reader.endObject();
        reader.endObject();
        reader.close();
        System.out.println(nprod+" products read, "+nrel+" added to catalog.");
    }
    

    
    /*
     * Check if the product is useful, to limit the size of the catalog and to exclude entries that are likely accessories
     */
    public static boolean qualifies(Product product) {
        if(product.getIndustry().equals("AU"))
            return false;
        
        boolean qualifies = true;
        
        return qualifies; // product.getIndustry().equals("CE"); //.. All products are from AU or CE.
    }
    
    private boolean matchesBlacklist(Product product) {
        for(String regex : blacklistWords) {
            if(product.getDescription().matches(regex)) {
                return true;
            }
        }
        return false;
    }
  
    /*
     * Reads a product encoded in JSON.
     */
    public static Product readProduct(JsonReader reader) throws IOException {
        if(reader.hasNext()) {
            String id = reader.nextName();
            reader.beginArray();
            String description = reader.nextString();
            String industry = reader.nextString();
            double price = reader.nextDouble();
            reader.endArray();
            Product result = new Product(id, description, industry, price);
            return result;
        }
        else
            return null;
    }
    
    /*
     * Reads all the products from a catalog file and builds an index from these.
     */
    public void buildIndex(String file) throws IOException {
        readProductCatalog(file);
        System.out.println("Building index...");
        int noProducts = 0;
        for(Product product : products) {
            if(noProducts%1000 ==0)
                System.out.println(noProducts+" products indexed.");
                
            String[] description = modifiedDescription(product).split("[ ,]");
            
            //Don't add words after 'for' to the index. These are not likely to be part of the product description
            //This helps to exclude accessories.
            for(int i=0; i<description.length && !description[i].equals("for"); i++) {
                addToIndex(description[i], product);
                String[] subTokens = description[i].split("[-/]");
                //All tokens are, if qualified, added to the index, but ignore tokens between brackets.
                boolean ignore = false;
                if(subTokens.length>1) {
                    for(String subToken : subTokens) {
                        if(subToken.length()>0&&subToken.charAt(0)=='(')
                            ignore = true;
                        if(!ignore)
                            addToIndex(subToken, product);
                        if(subToken.length()>0&&subToken.charAt(subToken.length()-1)==')')
                            ignore = false;
                    }
                }
            }
            
            noProducts++;
            
        }
        System.out.println("Done building index.");
    }
    
    /*
     * This method takes a token from a description in the product catalog as an input, 
     * and adds (a variation on) that token to the index.
     */
    public void addToIndex(String originalToken, Product product) {
        String token = BaselineModel.normalizeToken(originalToken);
        if(indexedToken(token)) { //Configurable?
            if(index.containsKey(token)) {
                index.get(token).add(product);
            }
            else {
                index.put(token, Sets.newHashSet(product));
            }
        }
    }
    
    /*
     * Modifies the description of a product by substituting patterns from a blacklist.
     */
    private String modifiedDescription(Product product) {
        String description = product.getDescription();
        for(String regex : blacklistSubstituteWords) {
            description.replace(regex, "");
        }
        
        return description;
    }
    
    /*
     * Returns whether a token should be indexed
     */
    public static boolean indexedToken(String token) {
        return token.length()>2;
    }

 
    
    //DEBUG
    public void printTokenSequence(String[] sequence) {
        for(String token : sequence)
            System.out.print(token+" ");
        System.out.println();
    }
    
    //DEBUG
    public void printIndex() {
        for(String key : index.keySet()) {
            System.out.print(key+":");
            for(Product p : index.get(key)) {
                System.out.print(" "+p.getId());
            }
            System.out.println();
        }
    }
    
    /*
     * Writes the index to a file, to get it back later
     */
    public void writeIndex(String file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for(String key : index.keySet()) {
            writer.write(key+":");
            for(Product p : index.get(key)) {
                writer.write(" "+p.getId());
            }
            writer.write("\n");
        }
        writer.close();
    }
    
    /*
     * Reads an index file, and adds the index pointers to the index
     */
    public void readIndexFromFile(String file) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(file));
        
        String line = reader.readLine();
        while(line!=null && line!=""){
            String[] indexLine;
            indexLine = StringUtils.split(line, ":");
            Set<Product> readProducts = Sets.newHashSet();
            String[] ids = StringUtils.split(indexLine[1], " ");
            for(String id : ids) {
                if(!id.equals(""))
                    readProducts.add(new Product(id, "MISSING", "MISSING", -1));
            }
            if(!readProducts.isEmpty()) {
                if(index.containsKey(indexLine[0]))
                    readProducts.addAll(index.get(indexLine[0]));
                index.put(indexLine[0], readProducts);
            }    
            line = reader.readLine();
        }
    }
    
    List<Product> getProducts(){
        return products;
    }
    
    public Map<String, Set<Product>> getIndex() {
        return index;
    }
}
