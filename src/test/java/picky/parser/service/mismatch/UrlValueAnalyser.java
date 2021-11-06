package picky.parser.service.mismatch;

import bepicky.service.entity.NewsNote;

public class UrlValueAnalyser implements ValueAnalyzer {

    @Override
    public String analyse(NewsNote expected, NewsNote actual) {
        return expected.getUrl().equals(actual.getUrl()) ? null : mismatchMsg(expected.getUrl(), actual.getUrl());
    }

    @Override
    public String key() {
        return "url";
    }
}
