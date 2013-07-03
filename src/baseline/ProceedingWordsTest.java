package baseline;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ProceedingWordsTest {

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        if(args.length==3) {
            TextCorpus trainingText = Baseline.readTextItemsFromJson(args[0]);
            Map<String, ArrayList<Annotation>> trueAnnotations = Baseline.readProductMentions(args[1]);
            RulebaseBaseline ruleModel = new RulebaseBaseline(args[2]);
            Map<String, ArrayList<Annotation>> ruleAnnotations = ruleModel.test(trainingText);
            Baseline.writeCSV(ruleAnnotations, "rulebase-training-annotations.csv");
            //Map<String, ArrayList<Annotation>> ruleAnnotations = Baseline.readProductMentions("rulebase-training-annotations.csv");
            writeMap(proceedingWordCount(trainingText, trueAnnotations),"true-proceedingwords.csv");
            writeMap(proceedingWordCount(trainingText, ruleAnnotations),"rule-proceedingwords.csv");
            writeMap(proceedingWordCount(trainingText, findOverlap(trueAnnotations, ruleAnnotations)),"overlap-proceedingwords.csv");
        }
        
        
        
    }
    
    private static Map<String, ArrayList<Annotation>> findOverlap(Map<String, ArrayList<Annotation>> annotations1, Map<String, ArrayList<Annotation>> annotations2) {
        Map<String, ArrayList<Annotation>> result = Maps.newHashMap();
        for(String textid : annotations1.keySet()) {
            if(annotations2.containsKey(textid)) {
                result.put(textid, new ArrayList<Annotation>());
                ArrayList<Annotation> list1 = annotations1.get(textid);
                ArrayList<Annotation> list2 = annotations2.get(textid);
                for(Annotation a : list1) {
                    for(Annotation b : list2) {
                        if(a.startToken==b.startToken && a.endToken==b.endToken){
                            result.get(textid).add(a);
                            break;
                        }
                    }
                }
            }
        }
        
        return result;
    }

    private static Map<String, Integer> proceedingWordCount(TextCorpus textCorpus, Map<String, ArrayList<Annotation>> annotations) {
        Map<String, Integer> result = Maps.newHashMap();
        
        for(String textId : annotations.keySet()){
            String[] text = textCorpus.get(textId);
            for(Annotation annotation : annotations.get(textId)) {
                if(annotation.startToken>0) {
                    String token = text[annotation.startToken-1].toLowerCase();
                    if(result.containsKey(token))
                        result.put(token, result.get(token)+1);
                    else
                        result.put(token, 1);
                }
            }
        }
        
        return result;
    }
    
    
    private static void writeMap(Map<String, Integer> map, String file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for(String token : map.keySet()) {
            writer.write("\""+token.replace("\"", "''")+"\"" + ","+map.get(token)+"\n");
            
        }
        writer.close();
    }
}
