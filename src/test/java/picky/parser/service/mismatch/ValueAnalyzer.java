package picky.parser.service.mismatch;

import bepicky.service.entity.NewsNote;

public interface ValueAnalyzer {

    String MISMATCH_PATTERN = "%s:expected: %s is different from actual: %s";

    String analyse(NewsNote expected, NewsNote actual);

    String key();

    default String mismatchMsg(String expected, String actual) {
        return String.format(MISMATCH_PATTERN, key(), expected, actual);
    }
}
