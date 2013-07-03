package baseline;

import java.util.List;

public class Featurizer {
    private final static String TOKEN_DELIMITER = " ";
    private final static String FEATURE_DELIMITER = " ";
    private final static String FEATURE_NAME_VALUE_DELIMITER = "_";
    
    public Featurizer() {
        
    }
    
    public String[] featurize(String[] textFile) {
        String[] result = new String[textFile.length];
        
        return result;
    }
    
    /*
     * Convert a sequence of tokens into a sequence of feature Strings.
     */
    public String[] featurize(String[] textFile, List<Feature> features, int tokenWindow) {
        String[] result = new String[textFile.length];
        
        //Compute for each token the corresponding feature values
        String[][] featurizedTokens = new String[textFile.length][features.size()];
        for(int i=0; i<textFile.length; i++) {
            result[i] = textFile[i];
            for(int j=0; j<features.size(); j++) {
                featurizedTokens[i][j] = features.get(j).evaluateFeature(textFile[i]);
            }
        }
        
        //Build the feature string, involving the surrounding tokens (according to the token window
        for(int i=0; i<textFile.length; i++) {
            StringBuilder featureString = new StringBuilder(textFile[i]);
            for(int j=i-tokenWindow; j<=i+tokenWindow; j++) {
                for(int f=0; f<features.size(); f++) {
                    //Example of one feature string: " -1CHARCNT_5"
                    featureString.append(FEATURE_DELIMITER);
                    featureString.append(j-i);
                    featureString.append(features.get(f).getName());
                    featureString.append(FEATURE_NAME_VALUE_DELIMITER);                                       
                    if(j>=0 && j<featurizedTokens.length)
                        featureString.append(featurizedTokens[j][f]);
                    else
                        featureString.append(0);
                }
                result[i] = featureString.toString();
            }
        }
        return result;
    }
 
  
}
