package picky.parser.service.doc;

import picky.parser.dto.ContentBlock;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picky.parser.dto.ContentTag;
import picky.parser.service.JsoupEvaluatorFactory;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
public class OnlyLinkDocumentTagParser implements DocumentTagParser {

    private final JsoupEvaluatorFactory evaluatorFactory;

    public OnlyLinkDocumentTagParser(JsoupEvaluatorFactory evaluatorFactory) {
        this.evaluatorFactory = evaluatorFactory;
    }

    @Override
    public Optional<Map.Entry<String, String>> parse(
        Element main, ContentBlock block, Function<Element, String> href
    ) {
        if (!matches(block)) {
            return Optional.empty();
        }
        ContentTag linkTag = block.findByType(ContentTag.Type.LINK);
        Element linkEl = main.selectFirst(evaluatorFactory.get(linkTag));
        return linkEl == null ? Optional.empty() : Optional.of(Map.entry(linkEl.text(), href.apply(linkEl)));
    }

    @Override
    public boolean matches(ContentBlock block) {
        ContentTag titleTag = block.findByType(ContentTag.Type.TITLE);
        ContentTag linkTag = block.findByType(ContentTag.Type.LINK);
        return titleTag == null && linkTag != null;
    }
}
