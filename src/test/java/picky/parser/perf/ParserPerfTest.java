package picky.parser.perf;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import picky.parser.dto.SourcePage;
import picky.parser.dto.TestParsedNews;
import picky.parser.service.UtilObjectMapper;
import picky.parser.service.approver.FuncContext;
import picky.test.NatsContainerSupport;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest
@Testcontainers
@Slf4j
public class ParserPerfTest implements NatsContainerSupport {

    @Autowired
    private Connection natsConnection;

    @Value("${subject.parse}")
    private String parseSubject;

    private final String replySubject = "replySubject";

    private final FuncContext sourcePageContext =
        new FuncContext(FuncContext.ContextType.SOURCE_PAGE);

    private final UtilObjectMapper om = new UtilObjectMapper();

    @Test
    public void test() throws InterruptedException {
        String sourceName = "укрправда";
        Set<SourcePage> sourcePages = sourcePageContext.getValueKeys(sourceName)
            .stream()
            .map(name -> sourcePageContext.get(sourceName, name))
            .map(data -> om.read(data, SourcePage.class))
            .collect(Collectors.toSet());

        Assertions.assertTrue(sourcePages.size() > 100);
        log.info("{} source pages to parse", sourcePages.size());

        Map<String, Long> urlWaitContainer = new HashMap<>();

        Dispatcher dispatcher = natsConnection.createDispatcher(msg -> {
            TestParsedNews rawNews = om.read(msg.getData(), TestParsedNews.class);
            urlWaitContainer.remove(rawNews.getUrl());
        });
        dispatcher.subscribe(replySubject);

        for (SourcePage sourcePage : sourcePages) {
            urlWaitContainer.put(sourcePage.getUrl(), System.currentTimeMillis());
            natsConnection.publish(parseSubject, replySubject, om.writeData(sourcePage));
        }

        long timeout = 15000;
        long startCountTime = System.currentTimeMillis();

        while (!urlWaitContainer.isEmpty()) {
            Thread.sleep(100);
            log.info("still waiting for {} pages to be parsed", urlWaitContainer.size());
            long timeSpent = System.currentTimeMillis() - startCountTime;
            if (timeSpent > timeout) {
                Assertions.fail((timeout / 1000) + " timeout reached. Not parsed: " + urlWaitContainer.keySet());
            }
        }
        long timeSpent = System.currentTimeMillis() - startCountTime;
        log.info("Perf passed! {} spent", timeSpent);
    }
}
