package picky.parser.config;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;

@Configuration
@Slf4j
public class NatsConfig {

    @Value("${nats.address}")
    private String natsAddress;

    @Bean
    public Connection natsConnection() throws IOException, InterruptedException {
        Options opt = new Options.Builder()
            .server("nats://" + natsAddress)
            .connectionName("na-ts")
            .connectionTimeout(Duration.ofSeconds(10))
            .build();
        return Nats.connect(opt);
    }

}
