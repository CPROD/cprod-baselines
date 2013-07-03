package baseline.filter;

import baseline.BaselineModel;
import baseline.Dictionary;
import baseline.RulebaseBaseline;

public class BlacklistFilter extends MentionFilter{

    private Dictionary commonWords;
    private Dictionary brands;
    
    
    public BlacklistFilter(Dictionary commonWords, Dictionary brands) {
        super();
        this.commonWords = commonWords;
        this.brands = brands;
    }



    @Override
    public boolean isValidMention(String[] text, int startIndex, int endIndex) {
        //if one token long then it should not match a blacklistpattern
        if(startIndex==endIndex && RulebaseBaseline.matchesBlacklistPattern(BaselineModel.normalizeToken(text[startIndex])))
            return false;
        
        // If 2-token long then if the 2nd token is a number then the 1st token must not be a lower cased dictionary word unless it is a known brand name
        if(startIndex+1==endIndex && isNumeric(text[endIndex]) && !brands.contains(text[startIndex]) &&commonWords.contains(text[startIndex]))
            return false;
        
        return true;
    }

}
