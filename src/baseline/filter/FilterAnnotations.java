package baseline.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import baseline.Annotation;
import baseline.Baseline;
import baseline.Dictionary;
import baseline.PrintAnnotations;
import baseline.TextCorpus;



public class FilterAnnotations {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        if(args.length>=4) {
            String textFile = args[0];
            String annotationsFile = args[1];
            String dictionaryFile = args[2];
            String resultFile = args[3];
            
            TextCorpus text = Baseline.readTextItemsFromJson(textFile);
            Map<String, ArrayList<Annotation>> annotations = Baseline.readProductMentions(annotationsFile);
            List<Dictionary> dics = Baseline.readDictionary(dictionaryFile);
            Dictionary commonWords = new Dictionary("ENCOMMONWORD");
            Dictionary brands = new Dictionary("BRANDNAME");
            for(Dictionary dict : dics) {
                if(dict.getName().equals("ENCOMMONWORD") || dict.getName().equals("GRAMMATICALWORD"))
                    commonWords.addAll(dict);
                if(dict.getName().equals("BRANDNAME"))
                    brands.addAll(dict);
            }
            
            MentionFilter filter = new MildMentionFilter(brands, commonWords);
            
            Map<String, ArrayList<Annotation>> filteredAnnotations = filter.filterMentions(annotations, text);
            
            Baseline.writeCSV(filteredAnnotations, resultFile);
            PrintAnnotations.main(new String[]{textFile, resultFile});
            
            if(args.length>4) {
                String truthFile = args[4];
                Map<String, ArrayList<Annotation>> truth = Baseline.readProductMentions(truthFile);
                Baseline.evaluateResult(filteredAnnotations, truth);
            }
        }
    }

}
