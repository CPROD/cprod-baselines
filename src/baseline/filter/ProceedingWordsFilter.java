package baseline.filter;

import java.util.Set;

import com.google.common.collect.Sets;
import baseline.BaselineModel;

public class ProceedingWordsFilter extends MentionFilter{
    public enum Strictness { BLACKLIST1TOKEN, BLACKLIST2TOKEN, ALLOWEDLIST}
    
    private Strictness strictness;
    
    public ProceedingWordsFilter(Strictness strictness) {
        this.strictness = strictness;
    }

    final Set<String> allowed = Sets.newHashSet(new String[]{
           // "the", "a", "an", "one", "my", "your", "his", "her", "our", "their", "this", "that"
            "new",
            "or",
            "the",
            "a",
            "<s>",
            "my",
            ",",
            "and"
          });
    
    final Set<String> blacklist1token = Sets.newHashSet(new String[]{
       "by"     
    });
    
    final Set<String> blacklist2token = Sets.newHashSet(new String[]{
            "quoted by", "edited by", "posted by"     
         });
    
    @Override
    public boolean isValidMention(String[] text, int startIndex, int endIndex) {
        switch(strictness)  {
        case BLACKLIST1TOKEN :  
            if(startIndex>0 && !blacklist1token.contains(BaselineModel.normalizeToken(text[startIndex-1])))
                return false;
            return true;
            
        case BLACKLIST2TOKEN :
            if(startIndex>1) {
                String token = BaselineModel.normalizeToken(text[startIndex-2]) + " " + BaselineModel.normalizeToken(text[startIndex-1]);
                return(!blacklist1token.contains(token));
            }
            return true;
            
        case ALLOWEDLIST :
            if(startIndex>1 && !allowed.contains(text[startIndex-1].toLowerCase()))
                    return false;
            return true;
        }
        return true;
    }
    
    

}
