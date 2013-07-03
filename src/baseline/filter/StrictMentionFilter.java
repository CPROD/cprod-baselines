package baseline.filter;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import baseline.Dictionary;
import baseline.filter.ProceedingWordsFilter.Strictness;

public class StrictMentionFilter extends MentionFilter{
    MentionFilter pipeline;
    
    
    public StrictMentionFilter(Dictionary brands, Dictionary commonWords) {
        List<MentionFilter> filters = Lists.newArrayList();
        filters.add(new ProceedingWordsFilter(Strictness.ALLOWEDLIST));
        filters.add(new MildMentionFilter(brands, commonWords));
        filters.add(new FirstTokenFilter());
        filters.add(new MentionLengthFilter(4, Integer.MAX_VALUE, 20));
        filters.add(new BrandnameFilter(brands));
        filters.add(new BlacklistFilter(commonWords, brands));
        
        
        pipeline = new FilterPipeline(filters);
    }

    @Override
    public boolean isValidMention(String[] text, int startIndex, int endIndex){
        return pipeline.isValidMention(text, startIndex, endIndex);
    }
}
