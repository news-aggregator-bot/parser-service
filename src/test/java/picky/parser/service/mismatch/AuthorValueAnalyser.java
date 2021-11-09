package picky.parser.service.mismatch;


import picky.parser.dto.ParsedNewsArticle;

public class AuthorValueAnalyser implements ValueAnalyzer {

    @Override
    public String analyse(ParsedNewsArticle expected, ParsedNewsArticle actual) {
        if (expected.getAuthor() != null && actual.getAuthor() != null) {
            return expected.getAuthor().equals(actual.getAuthor()) ?
                null :
                mismatchMsg(expected.getAuthor(), actual.getAuthor());
        }
        return null;
    }

    @Override
    public String key() {
        return "author";
    }
}
