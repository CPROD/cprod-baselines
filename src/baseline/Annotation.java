package baseline;

public class Annotation implements Comparable{

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((documentID == null) ? 0 : documentID.hashCode());
        result = prime * result + endToken;
        result = prime * result + startToken;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Annotation other = (Annotation) obj;
        if (documentID == null) {
            if (other.documentID != null)
                return false;
        } else if (!documentID.equals(other.documentID))
            return false;
        if (endToken != other.endToken)
            return false;
        if (startToken != other.startToken)
            return false;
        return true;
    }

    public String documentID;
    public int startToken, endToken;
    public String[] productIDs;
    public double confidence;
    
    public Annotation(String documentID, int startToken, int endToken, String[] productIDs, double confidence) {
        this.documentID = documentID;
        this.startToken = startToken;
        this.endToken = endToken;
        this.productIDs = productIDs;
        this.confidence = confidence;
    }
    
    public Annotation(String documentID, int startToken, int endToken, String[] productIDs) {
        this(documentID, startToken, endToken, productIDs, 1.0);
    }

    public int compareTo(Annotation a) {
        return startToken-a.startToken;
    }
    
    public int compareTo(Object o) {
        return 0;
    }
    
    public String toString() {
        String csvResult = documentID+":"+startToken+"-"+endToken+","+confidence+",";
        if(productIDs.length==0)
            csvResult += "0";
        else for(String productID : productIDs) {
            csvResult += productID + " ";
        }
        return csvResult;
    }
}
