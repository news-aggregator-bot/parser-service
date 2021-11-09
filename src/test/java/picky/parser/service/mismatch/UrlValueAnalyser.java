package picky.parser.service.mismatch;

import picky.parser.dto.ParsedNewsArticle;

public class UrlValueAnalyser implements ValueAnalyzer {

    @Override
    public String analyse(ParsedNewsArticle expected, ParsedNewsArticle actual) {
        return expected.getLink().equals(actual.getLink()) ? null : mismatchMsg(expected.getLink(), actual.getLink());
    }

    @Override
    public String key() {
        return "url";
    }
}
