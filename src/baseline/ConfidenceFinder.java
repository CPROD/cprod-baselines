package baseline;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class ConfidenceFinder {

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        if(args.length>0) {
            Config config = new Config();
            config.home_path = args[0];
            config.setRegex_feature_file("regexfeatures_basic.json");
            config.useDictionary = true;
            config.setFilePaths();
            
            TextCorpus textCorpus = Baseline.readTextItemsFromJson(config.getTest_text_file());
            
            Baseline2 baseline2 = new Baseline2(Baseline.makeFeatureList(config),1,"baseline2_basic_window5_model.crf");
            Set<Annotation> annotations = Baseline.aggregateAnnotations(baseline2.test(textCorpus));
            Set<Annotation> trueAnnotations = 
                Baseline.aggregateAnnotations(Baseline.readProductMentions(config.getGround_truth_annotations_file()));
            
            Writer out = new FileWriter(config.home_path+File.separator+"w5_confidences_gavg.txt");
            
            for(Annotation annotation : annotations) {
                int matches = trueAnnotations.contains(annotation)? 1 : 0;
                out.write(annotation.documentID+":"+annotation.startToken+"-"+annotation.endToken+","+
                        annotation.confidence+","
                        +matches+"\n");
            }
            
            
            out.close();
        }            
    }

}
