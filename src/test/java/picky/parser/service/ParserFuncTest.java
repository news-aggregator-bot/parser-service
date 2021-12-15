package picky.parser.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import picky.parser.dto.ParsedNews;
import picky.parser.dto.SourcePage;
import picky.parser.service.approver.FuncContext;
import picky.parser.service.mismatch.Mismatch;
import picky.parser.service.mismatch.ResultMismatchAnalyzer;
import picky.test.NatsContainerSupport;
import picky.test.WireMockSupport;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@Slf4j
@Testcontainers
@Disabled
public class ParserFuncTest extends WireMockSupport implements NatsContainerSupport {

    private static final String SOURCE_MISMATCH_PATTERN = "Source: %s\n%s";
    private static final String SOURCEPAGE_MISMATCH_PATTERN = "Source page: %s\n%s";

    private UtilObjectMapper om = new UtilObjectMapper();

    @Autowired
    private DefaultWebContentParser parser;

    private final FuncContext pageContext = new FuncContext(FuncContext.ContextType.WEB_PAGE);
    private final FuncContext sourcePageContext = new FuncContext(FuncContext.ContextType.SOURCE_PAGE);
    private final FuncContext newsContext = new FuncContext(FuncContext.ContextType.NEWS);
    private final ResultMismatchAnalyzer mismatchAnalyzer = new ResultMismatchAnalyzer();

    @Test
    public void funcTest() {
        Set<String> sourceNames = pageContext.getKeys();
        assertFalse(sourceNames.isEmpty());

        List<Pair<String, List<Pair<SourcePage, List<Mismatch>>>>> sourceMismatches = sourceNames
            .stream()
            .map(this::analyseSource)
            .filter(p -> !p.getValue().isEmpty())
            .collect(Collectors.toList());
        if (!sourceMismatches.isEmpty()) {
            String sourceErrorMsg = buildErrMsg(sourceMismatches);
            fail(sourceErrorMsg);
        }
    }

    private Pair<String, List<Pair<SourcePage, List<Mismatch>>>> analyseSource(String sourceName) {
        log.info("func:source:start:{}", sourceName);
        Set<String> sourcePageNames = pageContext.getValueKeys(sourceName);
        assertFalse(sourcePageNames.isEmpty());
        List<Pair<SourcePage, List<Mismatch>>> sourcePagesMismatches = sourcePageNames
            .stream()
            .map(sourcePageName -> analyseSourcePage(sourceName, sourcePageName))
            .filter(p -> !p.getValue().isEmpty())
            .collect(Collectors.toList());
        log.info("func:source:finish:{}", sourceName);
        return Pair.of(sourceName, sourcePagesMismatches);
    }

    private Pair<SourcePage, List<Mismatch>> analyseSourcePage(String sourceName, String sourcePageName) {
        SourcePage sourcePage = readSourcePage(sourceName, sourcePageName);
        String originalUrl = sourcePage.getUrl();
        log.info("func:sourcepage:start:{}", originalUrl);
        assertFalse(sourcePage.getContentBlocks().isEmpty());

        sourcePage.setUrl(replaceHost(sourcePage.getUrl()));
        String path = getPath(sourcePage.getUrl());
        stub(path, pageContext.get(sourceName, sourcePageName));
        ParsedNews expectedNews = readParsedNews(sourceName, sourcePageName);
        ParsedNews actualNews = parser.parse(sourcePage);
        stubVerify(path);

        List<Mismatch> mismatches = mismatchAnalyzer.analyse(expectedNews, actualNews);
        log.info("func:sourcepage:finish:{}", originalUrl);
        return Pair.of(sourcePage, mismatches);
    }

    private SourcePage readSourcePage(String sourceName, String sourcePageName){
        return om.read(
            sourcePageContext.get(sourceName.toLowerCase(), sourcePageName),
            SourcePage.class
        );
    }

    private ParsedNews readParsedNews(String sourceName, String sourcePageName){
        byte[] pageContent = newsContext.get(sourceName.toLowerCase(), sourcePageName);
        return om.read(pageContent, ParsedNews.class);
    }

    private String buildErrMsg(List<Pair<String, List<Pair<SourcePage, List<Mismatch>>>>> sourceMismatches) {
        return sourceMismatches.stream()
            .map(s -> {
                String sourcePageMismatchMsg = s.getValue().stream()
                    .filter(sp -> !sp.getValue().isEmpty())
                    .map(sp -> {
                        String mismatchMsg = sp.getValue()
                            .stream()
                            .filter(Objects::nonNull)
                            .map(Mismatch::toString)
                            .collect(Collectors.joining("\n"));
                        return String.format(SOURCEPAGE_MISMATCH_PATTERN, sp.getKey().getUrl(), mismatchMsg);
                    })
                    .collect(Collectors.joining("\n-----------------NEXT-SOURCE-PAGE------------------\n"));
                return String.format(SOURCE_MISMATCH_PATTERN, s.getKey(), sourcePageMismatchMsg);
            }).collect(Collectors.joining("\n-----------------NEXT-SOURCE-------------------\n"));
    }

}