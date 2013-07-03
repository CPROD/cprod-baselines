package baseline;

public class PatternFeature implements Feature{

    boolean compressedVersion;

    public PatternFeature(boolean compressedVersion) {
        super();
        this.compressedVersion = compressedVersion;
    }
    
    @Override
    public String evaluateFeature(String token) {
        String pattern = token.replace("[A-Z]", "A")
                                .replace("[a-z]", "a")
                                .replace("[0-9]", "0")
                                .replace("[^A-Za-z0-9]", "_");
        if(!compressedVersion)
            return pattern;
        
        pattern = pattern.replace("A+", "A")
                        .replace("a+", "a")
                        .replace("0+", "0")
                        .replace("_+", "_");
        
        return null;
    }

    @Override
    public String getName() {
       if(compressedVersion)
           return "PTRN_C";
       else
           return "PTRN";
    }

}
