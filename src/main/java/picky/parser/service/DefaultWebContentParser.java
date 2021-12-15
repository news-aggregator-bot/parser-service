package picky.parser.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import picky.parser.dto.ContentBlock;
import picky.parser.dto.ContentTag;
import picky.parser.dto.ParsedNews;
import picky.parser.dto.ParsedNewsArticle;
import picky.parser.dto.SourcePage;
import picky.parser.reader.WebPageReader;
import picky.parser.service.doc.DocumentTagParser;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DefaultWebContentParser implements WebContentParser {

    private final List<WebPageReader> webPageReaders;
    private final List<DocumentTagParser> tagParsers;
    private final JsoupEvaluatorFactory evaluatorFactory;
    private final UrlNormalisationService urlNormalisationService;
    private Map<String, WebPageReader> webPageReaderMap;

    public DefaultWebContentParser(
        List<WebPageReader> webPageReaders,
        List<DocumentTagParser> tagParsers,
        JsoupEvaluatorFactory evaluatorFactory,
        UrlNormalisationService urlNormalisationContext
    ) {
        this.webPageReaders = webPageReaders;
        this.tagParsers = tagParsers;
        this.evaluatorFactory = evaluatorFactory;
        this.urlNormalisationService = urlNormalisationContext;
    }

    @PostConstruct
    private void setWebPageReadersMap() {
        webPageReaderMap = webPageReaders.stream()
            .collect(Collectors.toMap(WebPageReader::name, Function.identity()));
    }

    @Override
    public ParsedNews parse(SourcePage page) {
        if (page.getWebReader() != null && webPageReaderMap.containsKey(page.getWebReader())) {
            WebPageReader spReader = webPageReaderMap.get(page.getWebReader());
            ParsedNews rawNewsNotes = getRawNews(page, spReader);
            if (rawNewsNotes != null) {
                return rawNewsNotes;
            }
        }
        for (WebPageReader webPageReader : webPageReaders) {
            ParsedNews rawNewsNotes = getRawNews(page, webPageReader);
            if (rawNewsNotes != null) {
                return rawNewsNotes;
            }
        }
        log.warn("webpageparser:read:empty:{}", page.getUrl());
        return new ParsedNews(Collections.emptySet(), page.getUrl(), null);
    }

    public String successfulRead(SourcePage page) {
        for (WebPageReader webPageReader : webPageReaders) {
            Document doc = readDocument(page, webPageReader).orElse(null);
            if (doc != null) {
                ParsedNews rawNewsNotes = parse(page, webPageReader, doc);
                if (rawNewsNotes != null) {
                    return doc.html();
                }
            }
        }
        return null;
    }

    private ParsedNews getRawNews(SourcePage page, WebPageReader webPageReader) {
        log.info(
            "webpageparser:{} :{}",
            page.getUrl(),
            webPageReader.getClass().getSimpleName()
        );
        return readDocument(page, webPageReader)
            .map(doc -> parse(page, webPageReader, doc))
            .orElse(null);
    }

    private ParsedNews parse(
        SourcePage page,
        WebPageReader webPageReader,
        Document doc
    ) {
        Set<ParsedNewsArticle> rawNewsNotes = page.getContentBlocks()
            .stream()
            .map(block -> parseDoc(page, doc, block))
            .flatMap(List::stream)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (rawNewsNotes.size() > 1) {
            return new ParsedNews(rawNewsNotes, page.getUrl(), webPageReader.name());
        }
        return null;
    }

    public Optional<Document> readDocument(SourcePage page, WebPageReader webPageReader) {
        try {
            return Optional.ofNullable(webPageReader.read(page.getUrl()));
        } catch (RuntimeException e) {
            if (!(e.getCause() instanceof org.jsoup.HttpStatusException)) {
                log.error("webpagereader:read:failed: {} : {}", page.getUrl(), e.getMessage());
            }
            return Optional.empty();
        }
    }

    private List<ParsedNewsArticle> parseDoc(SourcePage page, Document doc, ContentBlock block) {
        ContentTag mainTag = block.findByType(ContentTag.Type.MAIN);
        ContentTag authorTag = block.findByType(ContentTag.Type.AUTHOR);

        if (mainTag != null) {
            Elements mainClassElems = doc.select(evaluatorFactory.get(mainTag));

            Builder<ParsedNewsArticle> datas = ImmutableList.builder();
            for (Element main : mainClassElems) {

                tagParsers.stream()
                    .filter(tp -> tp.matches(block))
                    .map(tp -> tp.parse(main, block, a -> getHref(page, a)))
                    .filter(Optional::isPresent)
                    .findFirst()
                    .orElse(Optional.empty())
                    .ifPresent(tp -> datas.add(new ParsedNewsArticle(
                        tp.getKey(),
                        tp.getValue(),
                        getAuthor(authorTag, main)
                    )));
            }
            return datas.build();
        }
        return Collections.emptyList();
    }

    private String getHref(SourcePage page, Element a) {
        return urlNormalisationService.normaliseUrl(page, a);
    }

    private String getAuthor(ContentTag authorTag, Element wrapper) {
        if (authorTag != null) {
            Element element = wrapper.selectFirst(evaluatorFactory.get(authorTag));
            return element == null ? null : element.text();
        }
        return null;
    }
}
