package picky.parser.service.mismatch;

import lombok.Builder;
import lombok.Data;
import picky.parser.dto.ParsedNewsArticle;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
public class Mismatch {

    private ParsedNewsArticle expected;

    private ParsedNewsArticle actual;

    @Builder.Default
    private List<String> messages = new ArrayList<>();

    @Override
    public String toString() {
        return new StringBuilder("\n")
            .append("expected:").append(expected).append("\n")
            .append("actual:").append(actual).append("\n")
            .append("msgs:").append(messages).append("\n")
            .toString();
    }
}
