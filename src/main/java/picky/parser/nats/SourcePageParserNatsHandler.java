package picky.parser.nats;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import picky.parser.dto.ParsedNews;
import picky.parser.dto.SourcePage;
import picky.parser.service.WebContentParser;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class SourcePageParserNatsHandler {

    private final Connection natsConnection;

    private final ObjectMapper om;

    private final WebContentParser webContentParser;

    @Value("${subject.parse}")
    private String parserSubject;

    public SourcePageParserNatsHandler(
        Connection natsConnection,
        ObjectMapper om,
        WebContentParser webScrapperService
    ) {
        this.natsConnection = natsConnection;
        this.om = om;
        this.webContentParser = webScrapperService;
    }

    @PostConstruct
    public void createDispatcher() {
        Dispatcher dispatcher = natsConnection.createDispatcher(msg -> {
            long start = System.currentTimeMillis();
            try {
                SourcePage sp = om.readValue(msg.getData(), SourcePage.class);
                ParsedNews parsed = webContentParser.parse(sp);
                natsConnection.publish(
                    msg.getReplyTo(),
                    om.writeValueAsString(parsed).getBytes(StandardCharsets.UTF_8)
                );
                long total = System.currentTimeMillis() - start;
                log.info("web parser: {} :execution_time:{}", sp.getUrl(), total);
            } catch (IOException e) {
                log.error("web parser:failed: {}", msg.getData(), e);
            }
        });
        dispatcher.subscribe(parserSubject);
    }
}
