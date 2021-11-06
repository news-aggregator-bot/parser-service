package picky.parser.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode
@ToString(includeFieldNames = false)
public class ContentTag {

    private Type type;

    private String value;

    @JsonProperty("match_strategy")
    private MatchStrategy matchStrategy;

    public enum MatchStrategy {
        EQUALS, STARTS, CONTAINS, HTML_TAG
    }

    public enum Type {
        MAIN, TITLE, LINK, AUTHOR
    }
}

