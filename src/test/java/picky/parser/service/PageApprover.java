package picky.parser.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@SpringBootTest(classes = {NAService.class, PageApprover.PageApproverConfiguration.class})
@Slf4j
@Disabled
public class PageApprover {

    @Autowired
    private ISourcePageService sourcePageService;

    @Autowired
    private SourceIngestionService sourceIS;

    @Autowired
    private List<WebPageReader> webPageReaders;

    private FuncSourceDataIngestor dataIngestor;

    private PageContentContext pageContentContext;


    @BeforeEach
    public void setUpData() {
        dataIngestor = FuncSourceDataIngestor.builder()
            .sourceIS(sourceIS)
            .build();

        pageContentContext = new PageContentContext();
    }


    @Test
    public void approvePages() {
        log.info("ingest:source:start");
        dataIngestor.ingestSources();
        log.info("ingest:source:finish");

        sourcePageService.findAll()
            .stream()
            .filter(s -> !pageContentContext.exists(s.getSource().getName(), s.getUrl()))
            .forEach(sourcePage -> {
                log.info("read:sourcepage:start:{}", sourcePage.getUrl());
                Document pageData = readDocument(sourcePage);
                if (pageData == null) {
                    return;
                }
                log.info("read:sourcepage:complete:{}", sourcePage.getUrl());
                log.info("sync:sourcepage:start");
                Path syncResult = pageContentContext.approve(
                    sourcePage.getSource().getName(),
                    sourcePage.getUrl(),
                    pageData.html()
                );
                log.info("sync:sourcepage:complete:{}", syncResult.getFileName());
            });
    }

    private Document readDocument(SourcePage sourcePage) {
        return webPageReaders.stream()
            .map(reader -> {
                try {
                    return reader.read(sourcePage.getUrl());
                } catch (SourceException e) {
                    log.error("read:sourcepage:failed:{}", sourcePage.getUrl());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .max(Comparator.comparingInt(o -> o.html().length()))
            .orElse(null);
    }

}
