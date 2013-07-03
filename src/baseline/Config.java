package baseline;

import java.io.File;


public class Config {
    public enum ModelType {
        BASELINE1, BASELINE2, BASELINE3, RULEBASE
    };
    
    public enum FilterType {
        BLACKLIST, BRANDNAME, FIRST_TOKEN, MENTION_LENGTH, 
        PROCEEDINGWORDS_ALLOWED, PROCEEDINGWORDS_BLACKLIST1, PROCEEDINGWORDS_BLACKLIST2,
        SPECIAL_CHAR, MILD, STRICT
    }

    boolean train;
    boolean test;
    boolean filter;
    boolean link;
    boolean useDictionary;
    boolean usePatternFeatures;
    boolean useTokenFeature;
    boolean evaluate;

    int tokenWindow;

    String home_path;
    private String training_text_file;
    private String training_annotations_file;
    private String test_text_file;
    private String regex_feature_file;
    private String dictionary_file;
    private String annotation_result_file;
    private String ground_truth_annotations_file;
    private String index_file;
    private String product_dictionary_file;
    
    private FilterType[] filters;

    ModelType model;
    String model_name;

    public Config() {
        // default values. These are overwritten when deserialized from a JSON
        // file.
        model = ModelType.BASELINE1;
        model_name = "baseline1";

        train = true;
        test = true;
        useDictionary = true;
        usePatternFeatures = false;
        useTokenFeature = false;
        filter = false;
        link = false;
        evaluate = false;

        tokenWindow = 1;

        home_path = "";
        setFilePaths();
        
        filters = new FilterType[0];
    }

    public void setFilePaths() {
        training_text_file = "training-annotated-text.json";
        training_annotations_file = "training-disambiguated-product-mentions.120725.csv";
        test_text_file = "evaluation-text.json";
        regex_feature_file = "regexfeatures_test.json";
        dictionary_file = "dictionary.dat";
        annotation_result_file = model + "_results.csv";
        ground_truth_annotations_file = "evaluation-disambiguated-product-mentions.120725.csv";
        index_file = "index.txt";
    }

    public String getTraining_text_file() {
        return home_path + File.separator + training_text_file;

    }

    public void setTraining_text_file(String trainingTextFile) {
        training_text_file = trainingTextFile;
    }

    public String getTraining_annotations_file() {
        return home_path + File.separator + training_annotations_file;
    }

    public void setTraining_annotations_file(String trainingAnnotationsFile) {
        training_annotations_file = trainingAnnotationsFile;
    }

    public String getTest_text_file() {
        return home_path + File.separator + test_text_file;
    }

    public void setTest_text_file(String testTextFile) {
        test_text_file = testTextFile;
    }

    public String getRegex_feature_file() {
        return home_path + File.separator + regex_feature_file;
    }

    public void setRegex_feature_file(String regexFeatureFile) {
        regex_feature_file = regexFeatureFile;
    }

    public String getDictionary_file() {
        return home_path + File.separator + dictionary_file;
    }

    public void setDictionary_file(String dictionaryFile) {
        dictionary_file = dictionaryFile;
    }
    
    public String getProductDictionary_file() {
        if(product_dictionary_file==null)
            return null;
        return home_path + File.separator + product_dictionary_file;
    }

    public void setProductDictionary_file(String file) {
        product_dictionary_file = file;
    }

    public String getAnnotation_result_file() {
        return home_path + File.separator + annotation_result_file;
    }

    public void setAnnotation_result_file(String annotationResultFile) {
        annotation_result_file = annotationResultFile;
    }

    public String getGround_truth_annotations_file() {
        return home_path + File.separator + ground_truth_annotations_file;
    }

    public void setGround_truth_annotations_file(String groundTruthAnnotationsFile) {
        ground_truth_annotations_file = groundTruthAnnotationsFile;
    }
    
    public String getIndex_file() {
        return home_path + File.separator + index_file;
    }

    public void setIndex_file(String catalogFile) {
        index_file = catalogFile;
    }
    
    public FilterType[] getFilters(){
        return filters;
    }
}
