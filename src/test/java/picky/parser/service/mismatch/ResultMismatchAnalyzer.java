package picky.parser.service.mismatch;

import com.google.common.collect.ImmutableList;
import picky.parser.dto.ParsedNews;
import picky.parser.dto.ParsedNewsArticle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ResultMismatchAnalyzer {

    private final List<ValueAnalyzer> valueAnalyzers =
        ImmutableList.<ValueAnalyzer>builder()
            .add(new TitleValueAnalyser())
            .add(new UrlValueAnalyser())
            .add(new AuthorValueAnalyser())
            .build();

    public List<Mismatch> analyse(ParsedNews expectedNews, ParsedNews actualNews) {
        Set<ParsedNewsArticle> expected = expectedNews.getArticles();
        Set<ParsedNewsArticle> actual = actualNews.getArticles();
        if (expected.size() != actual.size()) {
            if (expected.size() > actual.size()) {
                Set<ParsedNewsArticle> expectedCopy = new HashSet<>(expected);
                expectedCopy.removeAll(actual);
                return expectedCopy.stream()
                    .map(n -> Mismatch.builder().expected(n).messages(Arrays.asList("actual is not exist")).build())
                    .collect(Collectors.toList());
            }
            Set<ParsedNewsArticle> actualCopy = new HashSet<>(actual);
            actualCopy.removeAll(expected);
            return actualCopy.stream()
                .map(n -> Mismatch.builder().actual(n).messages(Arrays.asList("expected is not exist")).build())
                .collect(Collectors.toList());
        }
        if (expected.equals(actual)) {
            return Collections.emptyList();
        }

        Map<String, List<ParsedNewsArticle>> expectedMap = expected.stream()
            .collect(Collectors.groupingBy(ParsedNewsArticle::getLink));
        Map<String, List<ParsedNewsArticle>> actualMap = actual.stream()
            .collect(Collectors.groupingBy(ParsedNewsArticle::getLink));

        return expectedMap.entrySet()
            .stream()
            .flatMap(e -> compare(e.getValue(), actualMap.get(e.getKey())).stream())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private List<Mismatch> compare(
        List<ParsedNewsArticle> expectedArticles, List<ParsedNewsArticle> actualArticles
    ) {
        List<Mismatch> articlesMismatches = new ArrayList<>();
        for (int i = 0; i < expectedArticles.size(); i++) {
            ParsedNewsArticle expectedArticle = expectedArticles.get(i);
            ParsedNewsArticle actualArticle = actualArticles != null ? actualArticles.get(i) : null;
            List<String> mismatchMessages = findMismatches(expectedArticle, actualArticle);
            if (!mismatchMessages.isEmpty()) {
                Mismatch mismatch = Mismatch.builder()
                    .expected(expectedArticle)
                    .actual(actualArticle)
                    .messages(mismatchMessages)
                    .build();
                articlesMismatches.add(mismatch);
            }
        }
        return articlesMismatches;
    }

    private List<String> findMismatches(ParsedNewsArticle expectedNote, ParsedNewsArticle actualNote) {
        if (actualNote == null) {
            return Arrays.asList("no actual note");
        }
        return valueAnalyzers.stream()
            .map(a -> a.analyse(expectedNote, actualNote))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
