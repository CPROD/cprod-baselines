package baseline.filter;

import org.apache.commons.lang.StringUtils;

import baseline.Dictionary;

public class SpecialCharacterFilter extends MentionFilter{

    public SpecialCharacterFilter() {
    }

    @Override
    public boolean isValidMention(String[] text, int startIndex, int endIndex) {
      //One of the tokens contains a special character
        String token = getMention(text, startIndex, endIndex);
        if(token.contains(",") || token.contains("!") || token.contains("^") || token.contains("&"))
            return false;
        return true;
    }

}
