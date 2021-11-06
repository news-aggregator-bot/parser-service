package picky.parser.service.doc;

import picky.parser.dto.ContentBlock;
import picky.parser.dto.ContentTag;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picky.parser.service.JsoupEvaluatorFactory;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
public class TitleLinkDocumentTagParser implements DocumentTagParser {

    @Autowired
    private JsoupEvaluatorFactory evaluatorFactory;

    @Override
    public Optional<Map.Entry<String, String>> parse(
        Element main, ContentBlock block, Function<Element, String> href
    ) {
        if (!matches(block)) {
            return Optional.empty();
        }
        ContentTag titleTag = block.findByType(ContentTag.Type.TITLE);
        ContentTag linkTag = block.findByType(ContentTag.Type.LINK);
        Element titleEl = main.selectFirst(evaluatorFactory.get(titleTag));
        if (titleEl == null) {
            return Optional.empty();
        }
        Element linkEl = main.selectFirst(evaluatorFactory.get(linkTag));
        if (linkEl == null) {
            return Optional.empty();
        }
        String link = href.apply(linkEl);
        if (StringUtils.isBlank(link)) {
            return Optional.empty();
        }
        return Optional.of(Map.entry(titleEl.text(), link));
    }

    @Override
    public boolean matches(ContentBlock block) {
        ContentTag titleTag = block.findByType(ContentTag.Type.TITLE);
        ContentTag linkTag = block.findByType(ContentTag.Type.LINK);
        return titleTag != null && linkTag != null;
    }
}
