package baseline;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexFeature implements Feature{
    public enum EvaluationType {BOOL, COUNT}
    
    private EvaluationType evaluationType;
    private String regex;
    private int cap;
    private String name;
    
    public RegexFeature(String name, EvaluationType evaluationType, String regex, int cap) {
        this.name = name;
        this.evaluationType = evaluationType;
        this.regex = regex;
        this.cap = cap;
    }
    
    public RegexFeature(String name, EvaluationType evaluationType, String regex) {
        this(name, evaluationType, regex, Integer.MAX_VALUE);
    }
    
    public RegexFeature() {}
    
    public String evaluateFeature(String token) {
        switch(evaluationType) {
            case BOOL   :   return evaluateBool(token)+"";
            case COUNT  :   return evaluateCount(token)+"";
            default     :   return "";
        }
    }
    
    private int evaluateCount(String token) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(token);
        int count = 0;
        while(matcher.find()) 
            count++;
        if(count <= cap)
            return count;
        else
            return 900+cap; //Make this configurable too?
    }
    
    private int evaluateBool(String token) {
        return token.matches(regex)? 1 : 0;
    }

    @Override
    public String getName() {
        return name;
    }
}
