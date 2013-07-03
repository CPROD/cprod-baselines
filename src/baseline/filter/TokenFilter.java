package baseline.filter;

import java.util.Set;

import com.google.common.collect.Sets;
import baseline.BaselineModel;
import baseline.Dictionary;

public class TokenFilter extends MentionFilter {

    Dictionary commonWords;
    Set<String> blacklist = Sets.newHashSet(new String[]{
            "<p>","<s>", "<P>", "<S>"
    });

    public TokenFilter(Dictionary commonWords) {
        this.commonWords = commonWords;
    }
    
    public TokenFilter(Dictionary commonWords, Dictionary blacklistWords) {
        this.commonWords = commonWords;
        blacklist.addAll(blacklistWords);
    }

    @Override
    public boolean isValidMention(String[] text, int startIndex, int endIndex) {

        return containsNonCommonWord(text, startIndex, endIndex) && 
                !containsBlacklistWord(text, startIndex, endIndex) && 
                containsAlphanumChars(text, startIndex, endIndex) &&
                firstCharAlphanumerical(text, startIndex);
    }

    //At least one of the tokens should not be a common word
    private boolean containsNonCommonWord(String[] text, int startIndex, int endIndex) {
        boolean result = false;
        for (int i = startIndex; i <= endIndex; i++) {
            if (!commonWords.contains(BaselineModel.normalizeToken(text[i])))
                result = true;
        }

        return result;
    }

    //If one of the tokens is in the blacklist, filter this mention
    private boolean containsBlacklistWord(String[] text, int startIndex, int endIndex) {
        for (int i = startIndex; i <= endIndex; i++) {
            if(blacklist.contains(text[i]))
                return true;
        }
        
        return false;
    }
    
    //At least one token should contain alphanumeric characters
    private boolean containsAlphanumChars(String[] text, int startIndex, int endIndex) {
        for(int i=startIndex; i<=endIndex; i++){
            String token = text[i];
            String normalizedToken = BaselineModel.normalizeToken(token);
            
            if(normalizedToken.length()==0)
                return false;
        }
        return true;
    }
    
    
    private boolean firstCharAlphanumerical(String[] text, int startIndex) {
        char startChar = text[startIndex].charAt(0);
        if(!(Character.isDigit(startChar) || Character.isLetter(startChar)))
            return false;
        
        return true;
    }
}
