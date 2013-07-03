package baseline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Merger {

    /*
     * This method merges two annotation lists, preferring annotations from the first list over the second.
     */
    public static Map<String, ArrayList<Annotation>> mergeUnion(Map<String, ArrayList<Annotation>> result1, Map<String, ArrayList<Annotation>> result2) {
        for(Map.Entry<String, ArrayList<Annotation>> annotations2 : result2.entrySet()) {
            if(result1.containsKey(annotations2.getKey())) {
                //Build a set of indices to tokens that are already annotated by result1
                Set<Integer> indices = new HashSet<Integer>();
                for(Annotation annotation : result1.get(annotations2.getKey())) {
                    for(int i=annotation.startToken; i<=annotation.endToken; i++) {
                        indices.add(i);
                    }
                }
                
                //Add those annotations that are not present yet
                for(Annotation annotation : annotations2.getValue()) {
                    boolean present = false;
                    for(int i=annotation.startToken; !present&& i<=annotation.endToken; i++) {
                        present = indices.contains(i);
                    }
                    if(!present)
                        result1.get(annotations2.getKey()).add(annotation);                    
                }
            }
            else {
                result1.put(annotations2.getKey(), annotations2.getValue());
            }
        }
        return result1;
    }
    
    
    public static Map<String, ArrayList<Annotation>> mergeVoting(List<Map<String, ArrayList<Annotation>>> solutions) {
        Map<String, ArrayList<Annotation>> result = Maps.newHashMap();
        Map<Integer, Integer> indices = Maps.newHashMap();
        return result;
    }
    
    //Merge overlaps: priority or longest common subsequence
}
