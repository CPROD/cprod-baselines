package baseline.filter;

public class MentionLengthFilter extends MentionFilter{

    int minLength, maxLength, maxTokenLength;
    
    public MentionLengthFilter(int minLength, int maxLength, int maxTokenLength) {
        super();
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.maxTokenLength = maxTokenLength;
    }

    @Override
    public boolean isValidMention(String[] text, int startIndex, int endIndex) {
        int totalLength = 0;
        for(int i=startIndex; i<=endIndex; i++) {
            String token = text[i];
            if(token.length()>maxTokenLength)
                return false;
            totalLength += token.length();
        }
        return totalLength<=maxLength && totalLength>=minLength;
    }

}
