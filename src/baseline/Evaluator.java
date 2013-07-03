package baseline;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.Sets;

public class Evaluator {
    Set<Annotation> predictedMentions;
    Set<Annotation> trueMentions;
    MeanFScoreAccumulator scoreEvaluator;
    
    public Evaluator(Set<Annotation> predictedMentions, Set<Annotation> trueMentions) {
        this.predictedMentions = predictedMentions;
        this.trueMentions = trueMentions;
        scoreEvaluator = new MeanFScoreAccumulator();
    }
    
    /*
     * Computes the F1 score for the mention recognition, without taking into account the links to the product catalog.
     */
    public double averageF1Mentions() {
        Set<String> predictedSet = getMentions(predictedMentions);
        Set<String> trueSet = getMentions(trueMentions);
        
        
        return scoreEvaluator.compute(predictedSet, trueSet).getAverageF1();
    }
    
    /*
     * Computes the average f1 score, where the f1 score for one annotation is equal to the f1 score for products, or 0 if there is no matching annotation.
     */
    public double averageF1Linking() {
        double average = 0;
        int matching = 0;
        for(Annotation annotation : predictedMentions) {
            Set<String> products = Sets.newHashSet();
            for(Annotation otherAnnotation : trueMentions) {
                if(annotation.documentID.equals(otherAnnotation.documentID)
                        &&  annotation.startToken==otherAnnotation.startToken
                        &&  annotation.endToken==otherAnnotation.endToken) {
                    products = Sets.newHashSet(otherAnnotation.productIDs);
                }
            }
            
            double f1 = scoreEvaluator.compute(Sets.newHashSet(annotation.productIDs), products).getAverageF1();
            average += f1;
            if(!products.isEmpty())
                matching ++;
        }
        
        double denominator = predictedMentions.size() + trueMentions.size() - matching;
        if(denominator>0)
            average /= denominator;
        else
            average = 0;
        
        return average;
    }
    
    /*
     * Returns a set of mentions, without the product references
     */
    private Set<String> getMentions(Set<Annotation> annotations) {
        Set<String> result = Sets.newHashSet();
        for(Annotation annotation : annotations)
                result.add(annotation.toString().split(",")[0]);
        return result;
    }
}
