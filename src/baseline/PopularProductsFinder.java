package baseline;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Maps;
import com.google.gson.stream.JsonReader;
/*
 * This class provides methods that read a product catalog file and extracts products and brand names that appear after the word 'for'.
 */
public class PopularProductsFinder {
    static final int MIN_PRODUCT_ENTRIES = 10;
    
    private Map<String, Integer> productsAfterFor;
    private Map<String, Integer> popularBrands;
    
    public PopularProductsFinder() {
        productsAfterFor = Maps.newHashMap();
        popularBrands = Maps.newHashMap();
    }
    
    public void findPopularProducts(String file) throws IOException {
        System.out.println("Reading products...");
        JsonReader reader = new JsonReader(new FileReader(file));
        reader.beginObject();
        reader.nextName(); // == "Product"
        reader.beginObject();
        int nprod=0, nrel=0; //DEBUG
        while(reader.hasNext()) {
            Product p = CatalogIndexer.readProduct(reader);
            if(p!=null) {
                updateProductsAfterFor(p);
                nrel++;
            }
            nprod++;
        }
        System.out.println("Done reading products. Now pruning...");
        pruneProductsAfterFor();
        System.out.println(nprod+" products read, "+nrel+" products relevant, "+productsAfterFor.size()+" products after for.");
        reader.endObject();
        reader.endObject();
        reader.close();
        
        writeProductsAndBrands("popular-products.csv", "popular-brands.csv");
    }
    
    /*
     * Reads the description of a product and extracts possible product names after 'for'
     */
    public String updateProductsAfterFor(Product product) {
        String result = "";
        String[] description = BaselineModel.normalizeToken(product.getDescription()).split("[ ,]");
        for(int i=0; i<description.length; i++) {
            if(description[i].equals("for")) {
                int j=i+2; //no unigrams
              //some heuristics: skip determiners
                if(i+1<description.length &&( description[i+1].equals("a") || description[i+1].equals("an") 
                        || description[i+1].equals("the") || description[i+1].equals("your"))) {
                    i++;
                    j++;
                }
                
                //Add bigrams and trigrams and 4grams after "for"
                while(j-i<=4 && j<description.length) {
                    String possibleProduct = StringUtils.join(Arrays.copyOfRange(description, i+1, j+1), " ");
                    result += possibleProduct +"\n";
                    if(productsAfterFor.containsKey(possibleProduct))
                        productsAfterFor.put(possibleProduct, productsAfterFor.get(possibleProduct)+1);
                    else
                        productsAfterFor.put(possibleProduct, 1);
                    
                    j++;
                }
            }
        }
        return result;
    }
    
    /*
     * Prunes the possible product names by retaining only the most popular.
     * Also extracts possible brand names.
     */
    public void pruneProductsAfterFor() {
        Map<String, Integer> popularProducts = Maps.newHashMap();
        Map<String, Integer> possibleBrandNames = Maps.newHashMap();
        popularProducts.putAll(productsAfterFor);
        for(String possibleProduct : productsAfterFor.keySet()) {
            String[] possibleProductArray = possibleProduct.split(" ");
            if(possibleProductArray.length>0) {
                String brand = possibleProductArray[0];
                if(possibleBrandNames.containsKey(brand))
                    possibleBrandNames.put(brand, possibleBrandNames.get(brand)+1);
                else
                    possibleBrandNames.put(brand, 1);
            }
            if(productsAfterFor.get(possibleProduct)<MIN_PRODUCT_ENTRIES) {
                popularProducts.remove(possibleProduct);
            }
        }
       //prune brands
        for(Map.Entry<String, Integer> brand : possibleBrandNames.entrySet()) {
            if(brand.getValue()>=MIN_PRODUCT_ENTRIES)
                popularBrands.put(brand.getKey(), brand.getValue());
        }
       
       productsAfterFor = popularProducts;
    }
    
    /*
     * Writes the popular products to a csv file
     */
    private void writePopularProducts(Map<String, Integer> popularProducts, String file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for(String product : popularProducts.keySet()) {
            writer.write(product + ","+popularProducts.get(product)+"\n");
            //System.out.println(product + ", "+popularProducts.get(product));
        }
        writer.close();
    }
    
    public void writeProductsAndBrands(String productsFile, String brandsFile) throws IOException {
        writePopularProducts(productsAfterFor, productsFile);
        writePopularProducts(popularBrands, brandsFile);
    }
}
