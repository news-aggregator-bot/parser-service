package picky.parser.service.mismatch;


import picky.parser.dto.ParsedNewsArticle;

public interface ValueAnalyzer {

    String MISMATCH_PATTERN = "%s:expected: %s is different from actual: %s";

    String analyse(ParsedNewsArticle expected, ParsedNewsArticle actual);

    String key();

    default String mismatchMsg(String expected, String actual) {
        return String.format(MISMATCH_PATTERN, key(), expected, actual);
    }
}
