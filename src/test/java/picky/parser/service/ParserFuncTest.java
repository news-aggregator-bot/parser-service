package picky.parser.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
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

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DefaultWebContentParser parser;

    private FuncContext pageContext = new FuncContext(FuncContext.ContextType.WEB_PAGE);

    private FuncContext newsContext = new FuncContext(FuncContext.ContextType.NEWS);

    private ResultMismatchAnalyzer mismatchAnalyzer = new ResultMismatchAnalyzer();

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
            .filter(Objects::nonNull)
            .filter(p -> !p.getValue().isEmpty())
            .collect(Collectors.toList());
        log.info("func:source:finish:{}", sourceName);
        return Pair.of(sourceName, sourcePagesMismatches);
    }

    private Pair<SourcePage, List<Mismatch>> analyseSourcePage(String sourceName, String sourcePageName) {
        SourcePage sourcePage = readSourcePage(sourceName, sourcePageName);
        log.info("func:sourcepage:start:{}", sourcePage.getUrl());
        assertFalse(sourcePage.getContentBlocks().isEmpty());

        ParsedNews expectedNews = readParsedNews(sourceName, sourcePageName);
        ParsedNews actuaNews = parser.parse(sourcePage);

        List<Mismatch> mismatches = mismatchAnalyzer.analyse(expectedNews, actuaNews);
        log.info("func:sourcepage:finish:{}", sourcePage.getUrl());
        return Pair.of(sourcePage, mismatches);
    }

    private SourcePage readSourcePage(String sourceName, String sourcePageName){
        try {
            byte[] pageContent = pageContext.get(sourceName.toLowerCase(), sourcePageName);
            return om.readValue(pageContent, SourcePage.class);
        } catch (IOException e) {
            throw new IllegalStateException("cannot read page " + sourcePageName);
        }
    }

    private ParsedNews readParsedNews(String sourceName, String sourcePageName){
        try {
            byte[] pageContent = newsContext.get(sourceName.toLowerCase(), sourcePageName);
            return om.readValue(pageContent, ParsedNews.class);
        } catch (IOException e) {
            throw new IllegalStateException("cannot read page " + sourcePageName);
        }
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