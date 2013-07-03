package baseline;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Feature {

    public String evaluateFeature(String token);
    public String getName();
}
