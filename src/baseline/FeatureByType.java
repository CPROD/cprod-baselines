package baseline;

import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Objects;

/*
 * This class contains a number of hard coded features. The specific feature is defined by the FeatureType.
 * NB: The dictionary features are not implemented, they need a dictionary.
 */
public class FeatureByType implements Feature {

    enum FeatureType {
        FIRST_CHARACTER("FRSTCHR"), CHAR_COUNT("CHARCNT"), UPPER_CASE_COUNT("UCCNT"), NUMBER_COUNT("NUMCNT"), LOWER_CASE_COUNT("LCCNT"), DASH_COUNT("DSHCNT"), SLASH_COUNT(
                "SLSHCNT"), PERIOD_COUNT("PERIODCNT"), DICTIONARY_GRAMMATICALWORD_COUNT("GRWRDCNT"), DICTIONARY_BRANDNAME_COUNT("BRNDWRDCNT"), DICTIONARY_ENCOMWORD_COUNT("ENWRDCNT");

        private final String value;

        private FeatureType(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }
    
    private FeatureType type;
    
    public FeatureByType(FeatureType type) {
        this.type = type;
    }
    
    @Override
    public String evaluateFeature(String token) {
        switch(type) {
        case FIRST_CHARACTER    :   return getFirstCharacterType(token);
        case CHAR_COUNT         :   return getCharCount(token)+"";
        case DASH_COUNT         :   return getDashCount(token)+"";
        case NUMBER_COUNT       :   return getNumbersCount(token)+"";
        case PERIOD_COUNT       :   return getPeriodCount(token)+"";
        case SLASH_COUNT        :   return getSlashCount(token)+"";
        case LOWER_CASE_COUNT   :   return getLowerCaseCount(token)+"";
        case UPPER_CASE_COUNT   :   return getUpperCaseCount(token)+"";
        case DICTIONARY_BRANDNAME_COUNT : return getBrandNameCount(token)+"";
        case DICTIONARY_ENCOMWORD_COUNT : return getCommonWordCount(token)+"";
        case DICTIONARY_GRAMMATICALWORD_COUNT : return getGrammaticalWordCount(token)+"";
        }
        return null;
    }
    
    private int getBrandNameCount(String token) {
        return getDictionaryTermCountBy();
    }
    
    private int getGrammaticalWordCount(String token){
        return getDictionaryTermCountBy();
    }
    
    private int getCommonWordCount(String token) {
        return getDictionaryTermCountBy();
    }

    private int getDictionaryTermCountBy() {
        int count = 0;
        return count;
    }
    
    private int getCharCount(String token) {
        int charCnt = token.length();
        return charCnt;
    }

    private int getCharCount2(String token) {
        int charCnt = token.length();
        if (charCnt >= 13) {
            charCnt = 913;
        }
        return charCnt;
    }

    private int getStartCharCount(String token, String regex) {
        return Pattern.matches(regex, token) ? 1 : 0;
    }

    private int getLowerCaseStartCharCount(String token) {
        return getStartCharCount(token, "^[a-z].*");
    }

    private int getNumberStartCharCount(String token) {
        return getStartCharCount(token, "^[0-9].*");
    }

    private int getUpperCaseStartCharCount(String token) {
        return getStartCharCount(token, "^[A-Z].*");
    }

    private int getCharCountWhenPatternRemoved(String token, String regex) {
        int cnt = token.replaceAll(regex, "").length();
        return cnt;
    }

    private int getUpperCaseCount(String token) {
        int ucCs = getCharCountWhenPatternRemoved(token, "[^A-Z]");
        return ucCs;
    }

    private int getUpperCaseCount2(String token) {
        int ucCs = getCharCountWhenPatternRemoved(token, "[^A-Z]");
        if (ucCs >= 5) {
            ucCs = 905;
        }
        return ucCs;
    }

    private int getLowerCaseCount(String token) {
        int lcLs = getCharCountWhenPatternRemoved(token, "[^a-z]");
        return lcLs;
    }

    private int getLowerCaseCount2(String token) {
        int lcLs = getCharCountWhenPatternRemoved(token, "[^a-z]");
        if (lcLs >= 11) {
            lcLs = 911;
        }
        return lcLs;
    }

    private int getNumbersCount(String token) {
        int numCs = getCharCountWhenPatternRemoved(token, "[^0-9]");
        return numCs;
    }

    private int getNumbersCount2(String token) {
        int numCs = getCharCountWhenPatternRemoved(token, "[^0-9]");
        if (numCs >= 6) {
            numCs = 906;
        }
        return numCs;
    }

    private int getDashCount(String token) {
        int dashCs = getCharCountWhenPatternRemoved(token, "[^\\-]");
        return dashCs;
    }

    private int getDashCount2(String token) {
        int dashCs = getCharCountWhenPatternRemoved(token, "[^\\-]");
        if (dashCs >= 2) {
            dashCs = 902;
        }
        return dashCs;
    }

    private int getSlashCount(String token) {
        int slashCs = getCharCountWhenPatternRemoved(token, "[^\\/]");
        return slashCs;
    }

    private int getSlashCount2(String token) {
        int slashCs = getCharCountWhenPatternRemoved(token, "[^\\/]");
        if (slashCs >= 2) {
            slashCs = 902;
        }
        return slashCs;
    }

    private int getPeriodCount(String token) {
        int periodCs = getCharCountWhenPatternRemoved(token, "[^\\.]");
        return periodCs;
    }

    private int getPeriodCount2(String token) {
        int periodCs = getCharCountWhenPatternRemoved(token, "[^\\.]");
        if (periodCs >= 2) {
            periodCs = 902;
        }
        return periodCs;
    }

    private String getFirstCharacterType(final String token) {
        if (getUpperCaseStartCharCount(token) > 0) {
            return "UC";
        } else if (getLowerCaseStartCharCount(token) > 0) {
            return "lc";
        } else if (getNumberStartCharCount(token) > 0) {
            return "num";
        } else {
            return "oth";
        }
    }

    @Override
    public String getName() {
        return type.toString();
    }
}
