package baseline;

public class NormalizedTokenFeature implements Feature {

    @Override
    public String evaluateFeature(String token) {
        return BaselineModel.normalizeToken(token);
    }

    @Override
    public String getName() {
        return "TOKEN";
    }

}
