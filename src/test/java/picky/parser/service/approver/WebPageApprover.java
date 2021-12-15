package picky.parser.service.approver;

import lombok.extern.slf4j.Slf4j;
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

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Slf4j
public class WebPageApprover {

    private static final JsoupEvaluatorFactory evalFactory = new JsoupEvaluatorFactory();
    private static final UtilObjectMapper om = new UtilObjectMapper();
    private static final List<WebPageReader> webPageReaders = List.of(
        new JsoupWebPageReader(),
        new HtmlUnitWebPageReader(30)
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

    public static void main(String[] args) throws IOException {
        Set<String> sourceNames = sourcePageContext.getKeys();
        sourceNames.parallelStream()
            .forEach(srcName -> {
            Set<String> sourcePagesNames = sourcePageContext.getValueKeys(srcName);
            for (String sourcePageName : sourcePagesNames) {
                if (webPageContext.exists(srcName, sourcePageName)) {
                    log.info("web page approve skip {}/{}", srcName, sourcePageName);
                    continue;
                }
                byte[] pageContent = sourcePageContext.get(srcName.toLowerCase(), sourcePageName);
                SourcePage sourcePage = om.read(pageContent, SourcePage.class);
                String html = parser.successfulRead(sourcePage);
                if (html == null) {
                    log.error("web page read failed {}", sourcePage.getUrl());
                } else {
                    webPageContext.approve(srcName, sourcePageName, html);
                    log.info("web page approved {}/{}", srcName, sourcePageName);
                }
            }
        });

    }

}
