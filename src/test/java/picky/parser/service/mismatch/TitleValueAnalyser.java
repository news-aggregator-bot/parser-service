package picky.parser.service.mismatch;


import picky.parser.dto.ParsedNewsArticle;

public class TitleValueAnalyser implements ValueAnalyzer {

    @Override
    public String analyse(
        ParsedNewsArticle expected, ParsedNewsArticle actual
    ) {
        return expected.getTitle().equals(actual.getTitle()) ?
            null :
            mismatchMsg(expected.getTitle(), actual.getTitle());
    }

    @Override
    public String key() {
        return "title";
    }
}
