package baseline.filter;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import baseline.BaselineModel;
import baseline.Dictionary;
import baseline.filter.ProceedingWordsFilter.Strictness;

public class MildMentionFilter extends MentionFilter {

    MentionFilter pipeline;
    
    
    public MildMentionFilter(Dictionary brands, Dictionary commonWords) {
        List<MentionFilter> filters = Lists.newArrayList();
        filters.add(new ProceedingWordsFilter(Strictness.BLACKLIST2TOKEN));
        filters.add(new TokenFilter(commonWords));
        filters.add(new SpecialCharacterFilter());
        filters.add(new MentionLengthFilter(3, Integer.MAX_VALUE, 20));
        
        pipeline = new FilterPipeline(filters);
    }

    @Override
    public boolean isValidMention(String[] text, int startIndex, int endIndex){
        return pipeline.isValidMention(text, startIndex, endIndex);
    }

}
