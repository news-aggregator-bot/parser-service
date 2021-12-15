package picky.parser.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@Data
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SourcePage {

    private String url;

    private String webReader;

    @EqualsAndHashCode.Exclude
    private String host;

    @JsonProperty("url_normalisation")
    private UrlNormalisation urlNormalisation;

    @JsonProperty("content_blocks")
    private Set<ContentBlock> contentBlocks;

    private boolean enabled;

}
