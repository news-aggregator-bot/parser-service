package picky.parser.service.approver;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import picky.parser.dto.SourcePage;
import picky.parser.dto.TestSource;
import picky.parser.service.UtilObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class SourcePageApprover {

    private static final FuncContext pageContentContext =
        new FuncContext(FuncContext.ContextType.SOURCE_PAGE);

    public static void main(String[] args) throws IOException {
        String sources = executeGet("/source/list");
        UtilObjectMapper om = new UtilObjectMapper();
        log.info("got sources JSON");
        List<TestSource> testSources = om.readTestSources(sources);
        log.info("converted sources");
        testSources.parallelStream()
            .forEach(testSource -> {

                log.info("read source {} pages", testSource.getName());
                String pagesJson = executeGet("/source/" + testSource.getId() + "/pages");
                if (pagesJson.isBlank()) {
                    return;
                }
                List<SourcePage> sourcePages = om.readSourcePages(pagesJson);
                log.info("got {} source {} pages", sourcePages.size(), testSource.getName());
                sourcePages.parallelStream()
                    .forEach(sourcePage -> {
                        if (pageContentContext.exists(testSource.getName(), sourcePage.getUrl())) {
                            return;
                        }
                        Path approved = pageContentContext.approve(
                            testSource.getName(),
                            sourcePage.getUrl(),
                            om.writeString(sourcePage)
                        );
                        log.info("approved {} source page", approved.getFileName());
                    });
            });

    }

    private static String executeGet(String url) {
        try {
            return Jsoup.connect("http://localhost:8081/god" + url)
                .header("Content-Type", "application/json")
                .ignoreContentType(true)
                .timeout(5000)
                .get()
                .text();
        } catch (IOException e) {
            log.error("approve source page failed");
            return "";
        }
    }

}
