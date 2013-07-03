package baseline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrintAnnotations {

    public static void main(String[] args) throws IOException {
        if(args.length==2) {
            String textFile = args[0];
            String annotationsFile = args[1];
            
            TextCorpus text = Baseline.readTextItemsFromJson(textFile);
            Map<String, ArrayList<Annotation>> annotations = Baseline.readProductMentions(annotationsFile);
            
            for(String textid : text.keySet()) {
                String[] tokens = text.get(textid);
                List<Annotation> textAnnotations = annotations.get(textid);
                if(textAnnotations!=null){
                    for(Annotation a : textAnnotations) {
                        System.out.print("\"");
                        StringBuilder tokenString = new StringBuilder();
                        for(int i=a.startToken; i<=a.endToken; i++) {
                            tokenString.append(tokens[i]+" ");
                        }
                        System.out.print(tokenString.toString().trim());
                        System.out.print("\"\t");
                        for(String id : a.productIDs) {
                            System.out.print(id+" ");
                        }
                        System.out.println("\t"+a.documentID+":"+a.startToken+"-"+a.endToken);
                    }
                }
            }
        }
    }
}
