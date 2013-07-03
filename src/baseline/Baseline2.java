package baseline;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import cc.mallet.fst.CRF;
import cc.mallet.fst.Segment;
import cc.mallet.fst.SimpleTagger;
import cc.mallet.fst.confidence.ConstrainedForwardBackwardConfidenceEstimator;
import cc.mallet.fst.confidence.GammaAverageConfidenceEstimator;
import cc.mallet.fst.confidence.GammaProductConfidenceEstimator;
import cc.mallet.fst.confidence.MaxEntConfidenceEstimator;
import cc.mallet.fst.confidence.RandomConfidenceEstimator;
import cc.mallet.fst.confidence.TransducerConfidenceEstimator;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;

public class Baseline2 extends BaselineModel {

    private Featurizer featurizer;
    private List<Feature> features;
    private final String TEMP_PATH  = "temp"+File.separator;
    private final String FEATURE_FILE = "derived-features.txt";
    private final String FEATURE_FILE_TEST = "test_features.txt";
    private final String MODEL_FILE = "_model.crf";
    private final String TAGGED_OUTPUT = "tags.txt";
    private int tokenWindow;
    private String modelName = "";
    
    public Baseline2(List<Feature> features, int tokenWindow) {
        featurizer = new Featurizer();
        this.features = features;
        this.tokenWindow = tokenWindow;
    }
    
    public Baseline2(List<Feature> features, int tokenWindow, String modelName) {
        this(features, tokenWindow);
        this.modelName = modelName;
    }
    
    @Override
    public Map<String, ArrayList<Annotation>> test(TextCorpus testCorpus) throws Exception {
        System.out.println("Testing Baseline2.");
        Map<String, ArrayList<Annotation>> annotations = new HashMap<String, ArrayList<Annotation>>();
        System.out.println("Featurizing test corpus...");
        PrintStream console = System.out;
        int noItems = 0; //DEBUG
        for(Map.Entry<String, String[]> text : testCorpus.entrySet()){
            System.out.print("Item "+noItems+" ");
            //featurize this text item
            TextCorpus featurizedCorpus = calculateFeatures(text.getKey(), text.getValue());
            File featureFile = new File(getFeatureFile(text.getKey()));
            StringWriter featureResult = new StringWriter();
            writeFeatureFile(featurizedCorpus, null, featureResult);        
            
            System.out.println("Start tagger...");
            //Compute the tags
            File tagfile = new File(text.getKey());
            PrintStream out = new PrintStream(new FileOutputStream(tagfile));
            
            
            //Code below is more or less copied from SimpleTagger
            ObjectInputStream s = new ObjectInputStream(new FileInputStream(getModelFilePath()));
            CRF crf = (CRF) s.readObject();
            Pipe p = crf.getInputPipe();
            p.setTargetProcessing(false);
            InstanceList testData = new InstanceList(p);
            Reader featureReader = new StringReader(featureResult.getBuffer().toString());
            //Reader featureReader = new FileReader(getFeatureFile(text.getKey()));
            testData.addThruPipe(new LineGroupIterator(featureReader, Pattern.compile("^\\s*$"), true));
            /*boolean includeInput = false;
            for (int i = 0; i < testData.size(); i++)
            {
              Sequence input = (Sequence)testData.get(i).getData();
              Sequence[] outputs = SimpleTagger.apply(crf, input, 1);
              int k = outputs.length;
              boolean error = false;
              for (int a = 0; a < k; a++) {
                if (outputs[a].size() != input.size()) {
                  System.err.println("Failed to decode input sequence " + i + ", answer " + a);
                  error = true;
                }
              }
              if (!error) {
                for (int j = 0; j < input.size(); j++)
                {
                   StringBuffer buf = new StringBuffer();
                  for (int a = 0; a < k; a++)
                     buf.append(outputs[a].get(j).toString()).append(" ");
                  if (includeInput) {
                    FeatureVector fv = (FeatureVector)input.get(j);
                    buf.append(fv.toString(true));                
                  }
                  out.println(buf.toString());
                }
                out.println();
              }
            }*/
            
            //Set target (to avoid error in SegmentIterator
            for(Instance ins : testData) {
                ins.unLock();
                ins.setTarget((Sequence) ins.getData ());
                ins.lock();
            }
          

            TransducerConfidenceEstimator estimator = new GammaAverageConfidenceEstimator(crf);
            Segment[] segments = estimator.rankSegmentsByConfidence(testData, new String[]{"B"}, new String[]{"I"});
            
            
            /*PrintStream out = new PrintStream(new FileOutputStream(tagfile));
            System.setOut(out);
            String[] args = {"--model-file", getModelFilePath(), getFeatureFile(text.getKey())};
            SimpleTagger.main(args);
            out.flush();
            out.close();
            System.setOut(console);*/

            
            //Convert the tags into annotations
            ArrayList<Annotation> textAnnotations = annotationsFromSegments(segments, text.getKey());
            out.close();
            //ArrayList<Annotation> textAnnotations = markAnnotations(text.getKey(), tagfile);
            if(!textAnnotations.isEmpty()) {
                annotations.put(text.getKey(), textAnnotations);
                for(Annotation a : textAnnotations) {
                    for(int i=a.startToken; i<=a.endToken; i++)
                        System.out.print(text.getValue()[i]+" ");
                    System.out.println(a.confidence);
                }
            }
            
            tagfile.delete();
            //featureFile.delete();
            noItems++;
        }
        
        return annotations;
    }
    
    private ArrayList<Annotation> annotationsFromSegments(Segment[] segments, String textId){
        ArrayList<Annotation> annotations = Lists.newArrayList();
        for(Segment s : segments) {
            if(s != null)
                annotations.add(new Annotation(textId, s.getStart(), s.getEnd(), new String[]{"0"}, s.getConfidence()));
        }
        return annotations;
    }
    
    /*
     * Constructs a list of annotations from a sequence of labels for the text.
     */
    private ArrayList<Annotation> markAnnotations(String id, File tagFile) throws IOException {
        ArrayList<Annotation> annotations = Lists.newArrayList();
        BufferedReader reader = new BufferedReader(new FileReader(tagFile));
        int currentToken = 0;
        String tag = reader.readLine();
        while(tag!=null&&!tag.equals("")){
            if(tag.charAt(0) == 'B') {
                int startPos = currentToken;
                tag = reader.readLine();
                currentToken++;
                while(tag!=null && !tag.equals("") && tag.charAt(0)=='I') {
                    currentToken++;
                    tag = reader.readLine();
                }
                Annotation annotation = new Annotation(id, startPos, currentToken-1, new String[]{"0"});
                annotations.add(annotation);
            }
            else { //tag == "O"
                currentToken++;
                tag = reader.readLine();
            }
        }
        reader.close();
        return annotations;
    }

    @Override
    void train(TextCorpus annotatedText, Map<String, ArrayList<Annotation>> annotations) throws Exception {
        System.out.println("Training Baseline2.");
        System.out.println("Featurizing corpus...");
        TextCorpus featurizedCorpus = calculateFeatures(annotatedText);
        System.out.println("Writing features to file...");
        writeFeatureFile(featurizedCorpus, annotations, new FileWriter(new File(FEATURE_FILE)));
        featurizedCorpus.clear();
        System.out.println("Train tagger...");
        String[] args = {"--train", "true", "--model-file", getModelFilePath() , FEATURE_FILE};
        SimpleTagger.main(args);

    }
    
    private String getModelFilePath() {
        return modelName+MODEL_FILE;
        //return modelName;
    }
    
    public String getFeatureFile(String id) {
        return TEMP_PATH+id+"_"+FEATURE_FILE;
    }
    
    /*
     * Calculates the feature strings for all text items in the corpus. 
     * The result is a corpus where each token is replaced by its feature string.
     */
    private TextCorpus calculateFeatures(TextCorpus annotatedText){
        TextCorpus featurizedCorpus = new TextCorpus();
        for(Map.Entry<String, String[]> textItem : annotatedText.entrySet()) {
            String[] featureVectors = featurizer.featurize(textItem.getValue(), features, tokenWindow);
            featurizedCorpus.put(textItem.getKey(), featureVectors);
        }
        return featurizedCorpus;
    }
    
    /*
     * Calculates the features for one text item (a list of tokens).
     */
    private TextCorpus calculateFeatures(String textid, String[] tokens) {
        TextCorpus corpus = new TextCorpus();
        corpus.put(textid, tokens);
        return calculateFeatures(corpus);
    }
    
    /*
     * Writes the features and labels (if known) of a text item to a file.
     * The format can be read by SimpleTagger.
     */
    private void writeFeatureFile(TextCorpus featurizedCorpus, Map<String, ArrayList<Annotation>> annotations, Writer writer) throws IOException {
        for(Map.Entry<String, String[]> textItem : featurizedCorpus.entrySet()) {
            int length = textItem.getValue().length;
            String[] labels;
            if(annotations!=null) {
                if(annotations.containsKey(textItem.getKey()))
                    labels = getLabels(annotations.get(textItem.getKey()), length);
                else
                    labels = getLabels(new ArrayList<Annotation>(), length);
            }                
            else
                labels = getLabels(null, length);
            
            for(int i=0; i<length; i++) {
                writer.write(textItem.getValue()[i]+" "+labels[i]+"\n");
            }
        }
        writer.close();
    }
    /*
     * Transforms a list of Annotations into a sequence of labels.
     */
    @VisibleForTesting
    protected String[] getLabels(List<Annotation> annotations, int length) {
        String[] labels = new String[length];
        for(int i=0; i<length; i++) {
            if(annotations!=null)
                labels[i] = "O";
            else
                labels[i] = "";
        }
        
        if(annotations!=null) {
            for(Annotation annotation : annotations)  {
                labels[annotation.startToken] = "B";
                for(int i=annotation.startToken+1; i<=annotation.endToken; i++) 
                    labels[i] = "I";
            }
        }
        
        return labels;
    }
    

}
