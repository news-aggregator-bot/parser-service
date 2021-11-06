package picky.parser.service.mismatch;

import bepicky.service.entity.NewsNote;

public class TitleValueAnalyser implements ValueAnalyzer {

    @Override
    public String analyse(
        NewsNote expected, NewsNote actual
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
