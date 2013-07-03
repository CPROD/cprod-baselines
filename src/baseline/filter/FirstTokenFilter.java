package baseline.filter;

public class FirstTokenFilter extends MentionFilter {

    @Override
    public boolean isValidMention(String[] text, int startIndex, int endIndex) {
        String token = text[startIndex];
        return firstCharAlphanumerical(token) && !isNumeric(token);
    }

    private boolean firstCharAlphanumerical(String token) {
        char startChar = token.charAt(0);
        if(!(Character.isDigit(startChar) || Character.isLetter(startChar)))
            return false;
        
        return true;
    }

}
