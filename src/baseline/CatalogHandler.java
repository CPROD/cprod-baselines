package baseline;

import java.io.IOException;

public class CatalogHandler {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        String catalogFile = "C:\\Users\\Daphne\\cprod_data\\products.json";
        String indexFile = "index.txt";
        boolean readIndexFromFile = false;
        boolean catalogStatistics = true;
        
        
        int i=0;
        while(i<args.length && args[i].startsWith("-")) {
            if(args[i].equals("-catalog-file")) {
                i++;
                if(i<args.length)
                    catalogFile = args[i];
            }
            else if(args[i].equals("-index-file")) {
                i++;
                if(i<args.length)
                    indexFile = args[i];
            }
            else if(args[i].equals("-read-index")) {
                i++;
                if(i<args.length)
                    readIndexFromFile = Boolean.parseBoolean(args[i]);
            }
            else if(args[i].equals("-catalog-statistics")) {
                i++;
                if(i<args.length)
                    catalogStatistics = Boolean.parseBoolean(args[i]);
            }
            else {
                System.err.println("Error: Invalid argument!");
                break;
            }
            i++;
        }
        
        CatalogIndexer indexer;
        
        if(readIndexFromFile) {
            indexer = new CatalogIndexer(indexFile);
        }
        else {
            indexer = new CatalogIndexer();
            if(catalogStatistics) {
                PopularProductsFinder finder = new PopularProductsFinder();
                finder.findPopularProducts(catalogFile);
                
            }
            else {
                indexer.buildIndex(catalogFile);
                indexer.writeIndex(indexFile);
            }
        }
    }
    
    

}
