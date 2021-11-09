package picky.parser.service.approver;

import lombok.extern.slf4j.Slf4j;
import picky.parser.dto.ParsedNews;
import picky.parser.dto.SourcePage;
import picky.parser.reader.HtmlUnitWebPageReader;
import picky.parser.reader.JsoupWebPageReader;
import picky.parser.reader.WebPageReader;
import picky.parser.service.DefaultWebContentParser;
import picky.parser.service.JsoupEvaluatorFactory;
import picky.parser.service.UrlNormalisationService;
import picky.parser.service.UtilObjectMapper;
import picky.parser.service.doc.DocumentTagParser;
import picky.parser.service.doc.OnlyLinkDocumentTagParser;
import picky.parser.service.doc.OnlyTitleDocumentTagParser;
import picky.parser.service.doc.TitleLinkDocumentTagParser;
import picky.parser.service.doc.TitleLinkNextToTagDocumentTagParser;
import picky.test.WireMockSupport;

import java.util.List;
import java.util.Set;

@Slf4j
public class ParsedNewsApprover {

    private static final UtilObjectMapper om = new UtilObjectMapper();

    private static final JsoupEvaluatorFactory evalFactory = new JsoupEvaluatorFactory();

    private static final List<WebPageReader> webPageReaders = List.of(
        new JsoupWebPageReader(),
        new HtmlUnitWebPageReader()
    );

    private static final List<DocumentTagParser> tagParsers = List.of(
        new OnlyLinkDocumentTagParser(evalFactory),
        new OnlyTitleDocumentTagParser(evalFactory),
        new TitleLinkDocumentTagParser(evalFactory),
        new TitleLinkNextToTagDocumentTagParser(evalFactory)
    );

    private static final DefaultWebContentParser parser = new DefaultWebContentParser(
        webPageReaders,
        tagParsers,
        evalFactory,
        new UrlNormalisationService()
    );

    private static final FuncContext sourcePageContext = new FuncContext(FuncContext.ContextType.SOURCE_PAGE);
    private static final FuncContext webPageContext = new FuncContext(FuncContext.ContextType.WEB_PAGE);
    private static final FuncContext newsContext = new FuncContext(FuncContext.ContextType.NEWS);

    public static void main(String[] args) {
        WireMockSupport wireMockServer = new WireMockSupport(){};
        WireMockSupport.startWireMock();
        try {
            for (String sourceName : sourcePageContext.getKeys()) {
                Set<String> sourcePages = sourcePageContext.getValueKeys(sourceName);
                for (String sourcePageName : sourcePages) {
                    byte[] sourcePageContent = sourcePageContext.get(sourceName, sourcePageName);
                    byte[] webPageContent = webPageContext.get(sourceName, sourcePageName);

                    SourcePage sourcePage = om.read(sourcePageContent, SourcePage.class);
                    sourcePage.setUrl(wireMockServer.replaceHost(sourcePage.getUrl()));

                    String path = wireMockServer.getPath(sourcePage.getUrl());
                    wireMockServer.stub(path, webPageContent);
                    ParsedNews parsedNews = parser.parse(sourcePage);
//                    if (parsedNews.getArticles().size() == 0) {
//                        throw new IllegalStateException("Single note on the whole page? " + sourcePage
//                            .getUrl());
//                    }
                    wireMockServer.stubVerify(path);

                    newsContext.approve(
                        sourceName,
                        sourcePage.getUrl(),
                        om.writeString(parsedNews)
                    );

                    log.info("parsed news approve completed {} {}", parsedNews.getArticles().size(), sourcePage.getUrl());
                }
            }
        } finally {
            WireMockSupport.stopWireMock();
        }
    }

}
