package baseline.filter;

import java.util.List;
import java.util.Set;

/*
 * This class is a MentionFilter that consists of a series of filters
 */
public class FilterPipeline extends MentionFilter {

    private List<MentionFilter> filterList;
    
    public FilterPipeline(List<MentionFilter> filterSet) {
        super();
        this.filterList = filterSet;
    }

    @Override
    public boolean isValidMention(String[] text, int startIndex, int endIndex) {
        for(MentionFilter filter : filterList) {
            if(!filter.isValidMention(text, startIndex, endIndex))
                return false;
        }
        return true;
    }

}
