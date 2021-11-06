package picky.parser.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import picky.parser.ITSupport;
import picky.parser.dto.ContentBlock;
import picky.parser.dto.ContentTag;
import picky.parser.dto.ParsedNews;
import picky.parser.dto.ParsedNewsArticle;
import picky.parser.dto.SourcePage;
import picky.parser.dto.UrlNormalisation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@SpringBootTest
public class DefaultWebContentParserITCase extends ITSupport {
    
    private static final String SOURCE_PAGE_DIR = "source-page/";

    @Autowired
    private WebContentParser webContentParser;

    @Autowired
    private ObjectMapper om;

    private static Stream<Arguments> data() {
        return Stream.of(
            Arguments.of("lowcost_news")
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    public void parse_ProvidedHtmlTag_WithProvidedSourcePage_ShouldReturnExpectedData(String name)
        throws JsonProcessingException {
        SourcePage sp = readSourcePage(name);
        Set<ParsedNewsArticle> expected = readExpected(name);


        ParsedNews parsed = webContentParser.parse(sp);

        Assertions.assertEquals(expected, parsed.getArticles());
    }

    private SourcePage readSourcePage(String sp) throws JsonProcessingException {
        return om.readValue(readData(SOURCE_PAGE_DIR+ sp + ".json"), SourcePage.class);
    }

    private Set<ParsedNewsArticle> readExpected(String name) throws JsonProcessingException {
        return om.readValue(
            readData(SOURCE_PAGE_DIR + "expected/" + name + ".json"),
            om.getTypeFactory().constructCollectionType(Set.class, ParsedNewsArticle.class)
        );
    }

    private String readPage(String page) {
        return readData("data/" + page);
    }

    private String readData(String page) {
        try {
            return IOUtils.toString(getClass().getResource(
                "/content/parser/" + page),
                StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Configuration
    @ComponentScan("picky.parser.service.doc")
    static class DefaultWebContentParserTestConfiguration {

        @Bean
        public WebContentParser webContentParser() {
            return new DefaultWebContentParser(
                List.of(),
                List.of(),
                evaluatorFactory(),
                urlNormalisationContext()
            );
        }

        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper om = new ObjectMapper();
            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return om;
        }

        @Bean
        public JsoupEvaluatorFactory evaluatorFactory() {
            return new JsoupEvaluatorFactory();
        }

        @Bean
        public UrlNormalisationContext urlNormalisationContext() {
            return new UrlNormalisationContext();
        }

    }
}