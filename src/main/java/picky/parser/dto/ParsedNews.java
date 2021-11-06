package picky.parser.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class ParsedNews {
    private final Set<ParsedNewsArticle> articles;

    private final String url;

    @JsonProperty("web_reader")
    private final String webReader;
}
