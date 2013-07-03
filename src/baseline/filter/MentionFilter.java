package baseline.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import baseline.Annotation;
import baseline.BaselineModel;
import baseline.Dictionary;
import baseline.RulebaseBaseline;
import baseline.TextCorpus;

public abstract class MentionFilter {
    
    public abstract boolean isValidMention(String[] text, int startIndex, int endIndex) ;
    

    public static boolean isNumeric(String token) {
        return token.matches("[0-9]+(\\.[0-9]+)?");
    }
    
    public Map<String, ArrayList<Annotation>> filterMentions(Map<String, ArrayList<Annotation>> annotations, TextCorpus corpus) {
        Map<String, ArrayList<Annotation>> result = Maps.newHashMap();
        for(String textid : annotations.keySet()) {
            ArrayList<Annotation> filteredAnnotations = Lists.newArrayList();
            for(Annotation annotation : annotations.get(textid)) {
                if(isValidMention(corpus.get(textid), annotation.startToken, annotation.endToken))
                    filteredAnnotations.add(annotation);
            }
            result.put(textid, filteredAnnotations);
        }
        
        return result;
    }
    
    protected String getMention(String[] text, int startIndex, int endIndex) {
        return StringUtils.join(Arrays.copyOfRange(text, startIndex, endIndex+1), " ");
    }
}
