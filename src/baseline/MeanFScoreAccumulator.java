package baseline;

import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * This stores the accumulated precision, recall and f1
 */
public class MeanFScoreAccumulator {
    private double precision = 0.0;
    private double recall = 0.0;
    private double f1 = 0.0;

    private long count = 0;

    public MeanFScoreAccumulator() {

    }

    public MeanFScoreAccumulator(double precision, double recall) {
        this.precision = precision;
        this.recall = recall;
        this.f1 = f1(precision, recall);
        this.count = 1;
    }

    public void accumulate(double precision, double recall) {
        this.precision += precision;
        this.recall += recall;
        this.f1 += f1(precision, recall);
        this.count += 1;
    }

    public void accumulate(MeanFScoreAccumulator other) {
        this.precision += other.precision;
        this.recall += other.recall;
        this.f1 += other.f1;
        this.count += other.count;
    }

    private double f1(double precision, double recall) {
        if (precision > 1E-8 && recall > 1E-8)
            return 2 * (precision * recall) / (precision + recall);
        else
            return 0.0;
    }

    public long getCount() {
        return count;
    }

    public double getAveragePrecision() {
        Preconditions.checkArgument(count > 0, "Nothing has been accumulated");
        return precision / count;
    }

    public double getAverageRecall() {
        Preconditions.checkArgument(count > 0, "Nothing has been accumulated");
        return recall / count;
    }

    public double getAverageF1() {
        Preconditions.checkArgument(count > 0, "Nothing has been accumulated");
        return f1 / count;
    }

    public static <T> MeanFScoreAccumulator compute(Set<T> predictedSet, Set<T> groundTruthSet) {
        int intersectionSize = Sets.intersection(predictedSet, groundTruthSet).size();
        int trueSize = groundTruthSet.size();
        int predictedSize = predictedSet.size();

        double precision;
        if (predictedSize == 0) {
            if (trueSize == 0)
                precision = 1d;
            else
                precision = 0d;
        } else {
            precision = intersectionSize / (double) predictedSize;
        }
        double recall = trueSize == 0 ? 1d : intersectionSize / (double) trueSize;
        return new MeanFScoreAccumulator(precision, recall);
    }
}
