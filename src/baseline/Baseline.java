package baseline;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import baseline.Config.FilterType;
import baseline.Config.ModelType;
import baseline.filter.*;
import baseline.filter.ProceedingWordsFilter.Strictness;

public class Baseline {
   
    public static void main(String[] args) throws Exception{
        System.out.println(java.lang.Runtime.getRuntime().maxMemory());
        String configFile;
        if(args.length>0) {
            configFile = args[0];
        }
        else {
            configFile="config.json";
        }
        
        Gson gson = new Gson();
        JsonReader jsonReader = new JsonReader(new FileReader(configFile));
        Config config = gson.fromJson(jsonReader, Config.class);
           
        BaselineModel model;
        
        switch(config.model){
        case BASELINE1  : model = new Baseline1(config.getProductDictionary_file());
                          break;
        
        case BASELINE2  : model = new Baseline2(makeFeatureList(config), config.tokenWindow, config.model_name);
                          break;
        
        case BASELINE3  : BaselineModel model1 = new Baseline1();
                          trainModel(model1, config);
                          model = new Baseline3(model1, new Baseline2(makeFeatureList(config), config.tokenWindow, config.model_name));
                          break;
        case RULEBASE  :  model = new RulebaseBaseline(config.getIndex_file());
                          break;
        
        default         : model = null;
        }
        
        if(config.train) {
            trainModel(model, config);
        }
        
        
        if(config.test) {
            TextCorpus testText = readTextItemsFromJson(config.getTest_text_file());
            Map<String, ArrayList<Annotation>> annotations = model.test(testText);
            if(config.filter) {
                System.out.println("Now filtering.");
                List<MentionFilter> filters = getFilters(config, readDictionary(config.getDictionary_file()));
                MentionFilter filterPipeline = new FilterPipeline(filters);
                annotations = filterPipeline.filterMentions(annotations, testText);
            }
            
            if(config.link) {
                System.out.println("Now linking.");
                annotations = link(config, annotations, testText);
            }
            writeCSV(annotations, config.getAnnotation_result_file());

        }
        
        if(!config.test && config.filter) {
            TextCorpus testText = readTextItemsFromJson(config.getTest_text_file());
            Map<String, ArrayList<Annotation>> annotations = readProductMentions(config.getAnnotation_result_file());
            List<MentionFilter> filters = getFilters(config, readDictionary(config.getDictionary_file()));
            MentionFilter filterPipeline = new FilterPipeline(filters);
            annotations = filterPipeline.filterMentions(annotations, testText);
            writeCSV(annotations, config.getAnnotation_result_file());
        }
        
        if(!config.test && config.link) {
            TextCorpus testText = readTextItemsFromJson(config.getTest_text_file());
            Map<String, ArrayList<Annotation>> annotations = readProductMentions(config.getAnnotation_result_file());
            
            writeCSV(link(config, annotations, testText), config.getAnnotation_result_file());
        }
        
        if(config.evaluate) {
            evaluateResult(config);
        }
    }
    
    public static Map<String, ArrayList<Annotation>> link(Config config, Map<String, ArrayList<Annotation>> annotations, TextCorpus text) throws IOException{
        CatalogLinker linker; 
        if(config.useDictionary) {
            List<Dictionary> dictionaries = readDictionary(config.getDictionary_file());
            Dictionary nonIndexedWords = new Dictionary("NONINDEX");
            for(Dictionary dict : dictionaries) {
                if(dict.getName().equals("ENCOMMONWORD") || dict.getName().equals("GRAMMATICALWORD"))
                    nonIndexedWords.addAll(dict);
            }
            
            linker = new CatalogLinker(config.getIndex_file(), nonIndexedWords);
        }
        else
            linker = new CatalogLinker(config.getIndex_file());
        linker.linkResults(annotations, text);
        
        return annotations;
    }
    
    public static void trainModel(BaselineModel model, Config config) throws Exception {
        TextCorpus annotatedText = readTextItemsFromJson(config.getTraining_text_file());
        Map<String, ArrayList<Annotation>> trainingMentions = readProductMentions(config.getTraining_annotations_file());
        model.train(annotatedText, trainingMentions);
    }
    
    /*
     * Reads a text item from a JSON file. The text item consists of the text ids and the lists of tokens.
     */
    public static TextCorpus readTextItemsFromJson(String file) throws IOException {
        System.out.println("Reading JSON files...");
        
        FileReader reader = new FileReader(file);
        JsonReader jsonReader = new JsonReader(reader);
        
        Gson gson = new Gson();
        Map<String, TextCorpus> text = gson.fromJson(reader, new TypeToken<Map<String, TextCorpus>>(){}.getType());
        jsonReader.close();
        return text.get("TextItem");
    }

    public static void writeCSV(Map<String, ArrayList<Annotation>> annotations, String file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for(Map.Entry<String, ArrayList<Annotation>> document : annotations.entrySet()) {
            ArrayList<Annotation> annotationList = document.getValue();
            for(Annotation annotation : annotationList) {
                writer.write(annotation.toString()+"\n");
            }
        }
        writer.close();
    }
    /*
     * Reads a csv file containing product mentions. 
     */
    public static Map<String, ArrayList<Annotation>> readProductMentions(String file) throws IOException {
        System.out.println("Reading csv file...");
        Map<String, ArrayList<Annotation>> mentions = new HashMap<String, ArrayList<Annotation>>();
        
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        while(line!=null){
            addMention(mentions, line);
            line = reader.readLine();
        }
        reader.close();
        return mentions;
    }
    
    /*
     * Adds a mention from a csv line to a list of Annotations.
     */
    public static void addMention(Map<String, ArrayList<Annotation>> mentions, String csvLine) {
        String[] split = csvLine.split(",");
        String doc = split[0];
        String products = split.length==2 ? split[1] : split[2];
        if(!doc.equals("id")){
            split = doc.split(":");
            String id = split[0];
            String tokenSpan = split[1];
            String[] tokenSpanSplit = tokenSpan.split("-");
            Annotation mention = new Annotation(id, Integer.parseInt(tokenSpanSplit[0]), Integer.parseInt(tokenSpanSplit[1]), products.split(" "));
            
            if(!mentions.containsKey(id))
                mentions.put(id, new ArrayList<Annotation>());
            mentions.get(id).add(mention);                
        }
    }
    
    /*
     * Reads a list of dictionary words from file
     */
    public static List<Dictionary> readDictionary(String file) throws IOException {
        Map<String, Dictionary> dictionaries = new HashMap<String, Dictionary>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        while(line!=null){
            String[] words = line.split("=");
            if(words.length==2) {
                if(!dictionaries.containsKey(words[1])) {
                    dictionaries.put(words[1],new Dictionary(words[1]));
                }
                dictionaries.get(words[1]).add(words[0]);
            }
            line=reader.readLine();
        }
        reader.close();
        Collection<Dictionary> dictValues = dictionaries.values();
        return new ArrayList<Dictionary>(dictValues);
    }
    
    public static List<Feature> readFeaturesFromJson(String file) throws IOException{
        FileReader reader = new FileReader(file);
        JsonReader jsonReader = new JsonReader(reader);
        
        Gson gson = new Gson();
        Feature[] features = gson.fromJson(reader, RegexFeature[].class);
        jsonReader.close();
        
        return Arrays.asList(features);
    }
    
    //Makes a list of features according to the config file
    public static List<Feature> makeFeatureList(Config config) throws IOException {
        ArrayList<Feature> features = new ArrayList<Feature>();
        features.addAll(readFeaturesFromJson(config.getRegex_feature_file()));
        if(config.useDictionary) {
            //add dictionary features
            List<Dictionary> dictionaries = readDictionary(config.getDictionary_file());
            for(Dictionary dict : dictionaries) {
                features.add(new DictionaryFeature(dict, dict.getName()));
            }
        }
        if(config.usePatternFeatures) {
            features.add(new PatternFeature(false));
            features.add(new PatternFeature(true));
        }
        if(config.useTokenFeature){
            features.add(new NormalizedTokenFeature());   
        }
        return features;
    }
    
    public static void evaluateResult(Config config) throws IOException{
        Map<String, ArrayList<Annotation>> predictedMentions = readProductMentions(config.getAnnotation_result_file());
        Map<String, ArrayList<Annotation>> trueMentions = readProductMentions(config.getGround_truth_annotations_file());
        
        evaluateResult(predictedMentions, trueMentions);
    }
    
    public static void evaluateResult( Map<String, ArrayList<Annotation>> predictedMentions,  Map<String, ArrayList<Annotation>> trueMentions) throws IOException{
        Evaluator eval = new Evaluator(aggregateAnnotations(predictedMentions), aggregateAnnotations(trueMentions));
        double f1Score1 = eval.averageF1Mentions();
        double f1Score2 = eval.averageF1Linking();
        System.out.println("f1score recognizer: "+f1Score1);
        System.out.println("f1score linker: "+f1Score2);
    }
    
    public static Set<Annotation> aggregateAnnotations(Map<String, ArrayList<Annotation>> annotations) {
        Set<Annotation> result = Sets.newHashSet();
        for(Entry<String, ArrayList<Annotation>> docAnnotations : annotations.entrySet())
            for(Annotation annotation : docAnnotations.getValue())
                result.add(annotation);
        
        return result;
    }
    
    public static List<MentionFilter> getFilters(Config config, List<Dictionary> dictionaries) {
        Dictionary commonWords = new Dictionary("ENCOMMONWORD");
        Dictionary brands = new Dictionary("BRANDNAME");
        for(Dictionary dict : dictionaries) {
            if(dict.getName().equals("ENCOMMONWORD") || dict.getName().equals("GRAMMATICALWORD"))
                commonWords.addAll(dict);
            if(dict.getName().equals("BRANDNAME"))
                brands.addAll(dict);
        }
        List<MentionFilter> filters = Lists.newArrayListWithExpectedSize(config.getFilters().length);
        
        for(FilterType type : config.getFilters()) {
            switch(type) {
            case BLACKLIST:
                filters.add(new BlacklistFilter(commonWords, brands));
                break;
            case BRANDNAME:
                filters.add(new BrandnameFilter(brands));
                break;
            case FIRST_TOKEN:
                filters.add(new FirstTokenFilter());
                break;
            case MENTION_LENGTH:
                filters.add(new MentionLengthFilter(4, 1000, 20)); //CONFIG
                break;
            case MILD:
                filters.add(new MildMentionFilter(brands, commonWords));
                break;
            case PROCEEDINGWORDS_ALLOWED:
                filters.add(new ProceedingWordsFilter(Strictness.ALLOWEDLIST));
                break;
            case PROCEEDINGWORDS_BLACKLIST1:
                filters.add(new ProceedingWordsFilter(Strictness.BLACKLIST1TOKEN));
                break;
            case PROCEEDINGWORDS_BLACKLIST2:
                filters.add(new ProceedingWordsFilter(Strictness.BLACKLIST2TOKEN));
                break;
            case SPECIAL_CHAR:
                filters.add(new SpecialCharacterFilter());
                break;
            case STRICT:
                filters.add(new StrictMentionFilter(brands, commonWords));
                break;
            
            }
        }
        return filters;
    }
    
    
}

