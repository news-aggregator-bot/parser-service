package picky.parser.service.approver;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import picky.parser.dto.SourcePage;
import picky.parser.reader.HtmlUnitWebPageReader;
import picky.parser.reader.JsoupWebPageReader;
import picky.parser.reader.WebPageReader;
import picky.parser.service.UtilObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Slf4j
public class WebPageApprover {

    private static final FuncContext sourcePageContext = new FuncContext(FuncContext.ContextType.SOURCE_PAGE);
    private static final FuncContext webPageContext = new FuncContext(FuncContext.ContextType.WEB_PAGE);
    private static final UtilObjectMapper om = new UtilObjectMapper();
    private static final JsoupWebPageReader jsoupWebPageReader = new JsoupWebPageReader();
    private static final HtmlUnitWebPageReader htmlUnitWebPageReader = new HtmlUnitWebPageReader();
    private static final List<WebPageReader> webPageReaders = List.of(
        jsoupWebPageReader,
        htmlUnitWebPageReader
    );

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
                String html = readWebPage(sourcePage.getUrl());
                if (html == null) {
                    log.error("web page read failed {}", sourcePage.getUrl());
                } else {
                    webPageContext.approve(srcName, sourcePageName, html);
                    log.info("web page approved {}/{}", srcName, sourcePageName);
                }
            }
        });

    }

    private static String readWebPage(String url){
        for (WebPageReader webPageReader : webPageReaders) {
            try {
                Document doc = webPageReader.read(url);
                if (doc != null) {
                    return doc.html();
                }
            } catch (Exception e) {
                log.error("web page read failed {}", url);
            }
        }
        return null;
    }

}
