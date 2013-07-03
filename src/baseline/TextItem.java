package baseline;
import java.util.*;

public class TextItem implements Map.Entry<String, String[]>{

    private String id;
    private String[] text;

    @Override
    public String getKey() {
        return id;
    }

    @Override
    public String[] getValue() {
       return text;
    }

    @Override
    public String[] setValue(String[] arg0) {
        text = arg0;
        return text;
    }
}
