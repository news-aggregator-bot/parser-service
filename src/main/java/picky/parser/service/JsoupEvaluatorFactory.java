package picky.parser.service;

import com.google.common.collect.ImmutableMap;
import org.jsoup.select.Evaluator;
import org.springframework.stereotype.Component;
import picky.parser.dto.ContentTag;

import java.util.Map;
import java.util.function.Function;

@Component
public class JsoupEvaluatorFactory {

    private final Map<ContentTag.MatchStrategy, Function<String, Evaluator>> container =
        ImmutableMap.<ContentTag.MatchStrategy, Function<String, Evaluator>>builder()
            .put(ContentTag.MatchStrategy.HTML_TAG, Evaluator.Tag::new)
            .put(ContentTag.MatchStrategy.EQUALS, Evaluator.Class::new)
            .put(ContentTag.MatchStrategy.STARTS, v -> new Evaluator.AttributeWithValueContaining("class", v + " "))
            .build();


    public Evaluator get(ContentTag tag) {
        return container.get(tag.getMatchStrategy()).apply(tag.getValue());
    }

    public Evaluator get(ContentTag.MatchStrategy strategy, String value) {
        return container.get(strategy).apply(value);
    }
}
