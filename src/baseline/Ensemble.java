package baseline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class Ensemble {

    public static void main(String[] args) throws IOException {
        if(args.length>0) {
            Map<String, ArrayList<Annotation>> annotations1 = Baseline.readProductMentions(args[0]);
            Map<String, ArrayList<Annotation>> annotations2 = Baseline.readProductMentions(args[1]);
            Map<String, ArrayList<Annotation>> truth = Baseline.readProductMentions(args[2]);
            
            Map<String, ArrayList<Annotation>> result = Merger.mergeUnion(annotations1, annotations2);
            Baseline.writeCSV(result, args[3]);
            Baseline.evaluateResult(result, truth);
        }
    }
}
