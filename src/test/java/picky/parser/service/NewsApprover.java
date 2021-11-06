package picky.parser.service;

import bepicky.service.data.ingestor.service.SourceIngestionService;
import bepicky.service.entity.NewsNote;
import bepicky.service.entity.Source;
import bepicky.service.entity.SourcePage;
import bepicky.service.service.INewsAggregationService;
import bepicky.service.service.ISourcePageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.PostConstruct;
import java.util.Set;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@ActiveProfiles("it")
@Disabled
public class NewsApprover {

    @Autowired
    private INewsAggregationService newsService;

    @Autowired
    private SourceIngestionService sourceIS;

    @Autowired
    private ISourcePageService sourcePageService;

    @Autowired
    private ObjectMapper objectMapper;

    private FuncSourceDataIngestor dataIngestor;

    private PageContentContext pageContentContext;

    private NewsNoteContext newsContext;

    @PostConstruct
    public void setSourceData() {
        dataIngestor = FuncSourceDataIngestor.builder()
            .sourceIS(sourceIS)
            .build();

        pageContentContext = new PageContentContext();
        newsContext = new NewsNoteContext(objectMapper);
    }

    @Test
    public void approveNews() {
        log.info("ingest:source:start");
        dataIngestor.ingestSources();
        log.info("ingest:source:finish");
        sourcePageService.findAll().forEach(this::analyseSourcePage);
    }


    private void analyseSourcePage(SourcePage sourcePage) {
        Source source = sourcePage.getSource();
        log.info("approve:sourcepage:start:{}", sourcePage.getUrl());
        assertFalse(sourcePage.getContentBlocks().isEmpty());

        byte[] pageContent = pageContentContext.get(source.getName().toLowerCase(), sourcePage.getUrl());
        String path = "getPath(sourcePage)";
        Set<NewsNote> freshNews = Set.of();
        if (freshNews.size() <= 1) {
            throw new IllegalStateException("Single note on the whole page? " + sourcePage.getUrl());
        }

        newsContext.approve(sourcePage.getSource().getName(), sourcePage.getUrl(), freshNews);

        log.info("approve:sourcepage:finish:{}", sourcePage.getUrl());
    }

}
