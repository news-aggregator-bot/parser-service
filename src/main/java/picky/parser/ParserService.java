package picky.parser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "picky.parser")
public class ParserService {

    public static void main(String[] args) {
        SpringApplication.run(ParserService.class, args);
    }
}
