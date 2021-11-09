package picky.parser.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import picky.parser.dto.ParsedNewsArticle;

import java.util.Set;

@Data
public class TestParsedNews {
    private Set<ParsedNewsArticle> articles;

    private String url;

    @JsonProperty("web_reader")
    private String webReader;
}
