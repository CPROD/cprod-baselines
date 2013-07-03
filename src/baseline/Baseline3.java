package baseline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * This baseline is a combination of Baseline1 and Baseline2, where annotations from Baseline1 are preferred over those from Baseline2.
 */
public class Baseline3 extends BaselineModel{
    
    private BaselineModel model1, model2;
    
    public Baseline3(BaselineModel model1, BaselineModel model2) {
        this.model1 = model1;
        this.model2 = model2;
    }

    @Override
    Map<String, ArrayList<Annotation>> test(TextCorpus textitem) throws Exception {
        Map<String, ArrayList<Annotation>> result1 = model1.test(textitem);
        Map<String, ArrayList<Annotation>> result2 = model2.test(textitem);
        Map<String, ArrayList<Annotation>> result = Merger.mergeUnion(result1, result2);
        return result;
    }
    


    void train(TextCorpus annotatedText, Map<String, ArrayList<Annotation>> annotations) throws Exception {
        model1.train(annotatedText, annotations);
        model2.train(annotatedText, annotations);
    }

}
