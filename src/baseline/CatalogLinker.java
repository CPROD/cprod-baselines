package baseline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

public class CatalogLinker {

    private Map<String, Set<Product>> index;

    private Dictionary nonIndexedWords;
    
    /*
     * The index is loaded from a file
     */
    public CatalogLinker(String indexFile) throws IOException {
        CatalogIndexer indexer = new CatalogIndexer(indexFile);
        index = indexer.getIndex();
    }
    
    /*
     * Optional: a dictionary of words that are not indexed (or should not be looked up in the index)
     */
    public CatalogLinker(String indexFile, Dictionary nonIndexedWords) throws IOException {
        this(indexFile);
        this.nonIndexedWords = nonIndexedWords;
    }
   
    
    /*
     * Gets all the relevant products from the catalog for this mention
     */
    public Collection<Product> getProductReferences(String[] mention) {
        Set<Product> references = null;
        Set<Product>[] referencesPerToken = new Set[mention.length];
        
        //for each token in the mention, look up the indexed products
        for(int i=0; i<mention.length; i++) {
            String normalizedToken = BaselineModel.normalizeToken(mention[i]);
            if(indexedToken(normalizedToken)) {
                Set<Product> containingThisToken = index.get(normalizedToken);
                if(containingThisToken==null) 
                    containingThisToken = Sets.newHashSet();
                referencesPerToken[i] = containingThisToken;
            }
        }
        
        //Take the intersection of all defined sets of products.
        if(mention.length>0)
            references = referencesPerToken[0];
        for(int i=1; i<mention.length; i++) {
            if(references==null)
                references = referencesPerToken[i];
            else if(referencesPerToken[i]!=null)
                references.retainAll(referencesPerToken[i]);
        }
        
        if(references==null)
            references = Sets.newHashSet();
        
        //Do some more filtering?
        if(references.size()>Integer.MAX_VALUE ) {
            printTokenSequence(mention);
            references.clear();
        }
            
        
        return references;
    }
    
    /*
     * Tells wheather this word should be looked up in the index.
     */
    private boolean indexedToken(String token) {
        if(nonIndexedWords!=null && nonIndexedWords.contains(token))
            return false;
        return CatalogIndexer.indexedToken(token);
    }
    
    /*
     * Returns all products that are referenced in the index
     */
    private Set<Product> getAllProducts() {
        Set<Product> allProducts = Sets.newHashSet();
        for(Set<Product> products : index.values())
            allProducts.addAll(products);
        
        return allProducts;
    }
    
    //DEBUG
    public void printTokenSequence(String[] sequence) {
        for(String token : sequence)
            System.out.print(token+" ");
        System.out.println();
    }
    
    /*
     * Links a set of mentions to the corresponding products in the catalog
     */
    public Map<String, ArrayList<Annotation>> linkResults(Map<String, ArrayList<Annotation>> annotations, TextCorpus corpus) {
        for(String textId : annotations.keySet()) {
            String[] text = corpus.get(textId);
            for(Annotation annotation : annotations.get(textId)) {
                String[] mention = Arrays.copyOfRange(text, annotation.startToken, annotation.endToken+1);
                Set<String> indexedProducts = Sets.newHashSet();
                Collection<Product> products = getProductReferences(mention);
                for(Product product : products)
                    indexedProducts.add(product.getId());
                
                Set<String> annotatedProductsIds = Sets.newHashSet(annotation.productIDs);
                Set<String> mergedProducts = merge(annotatedProductsIds, indexedProducts);
                annotation.productIDs = mergedProducts.toArray(new String[mergedProducts.size()]);
            }
        }
        return annotations;
    }
    
    public Set merge(Set set1, Set set2) {
        if(set1.size()==1 && !set2.isEmpty())  return set2;
        else if(set2.isEmpty()) return set1;
        else return set1; //PREFER SET1! can also be changed to union of both sets
    }
}
