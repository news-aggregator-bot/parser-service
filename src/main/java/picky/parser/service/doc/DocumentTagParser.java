package picky.parser.service.doc;

import org.jsoup.nodes.Element;
import picky.parser.dto.ContentBlock;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface DocumentTagParser {

    Optional<Map.Entry<String, String>> parse(Element main, ContentBlock block, Function<Element, String> href);

    boolean matches(ContentBlock block);
}
