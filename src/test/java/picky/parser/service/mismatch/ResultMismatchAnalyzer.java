package picky.parser.service.mismatch;

import bepicky.service.entity.NewsNote;
import com.google.common.collect.ImmutableList;

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

    public List<Mismatch> analyse(Set<NewsNote> expected, Set<NewsNote> actual) {
        if (expected.size() != actual.size()) {
            if (expected.size() > actual.size()) {
                Set<NewsNote> expectedCopy = new HashSet<>(expected);
                expectedCopy.removeAll(actual);
                return expectedCopy.stream()
                    .map(n -> Mismatch.builder().expected(n).messages(Arrays.asList("actual is not exist")).build())
                    .collect(Collectors.toList());
            }
            Set<NewsNote> actualCopy = new HashSet<>(actual);
            actualCopy.removeAll(expected);
            return actualCopy.stream()
                .map(n -> Mismatch.builder().actual(n).messages(Arrays.asList("expected is not exist")).build())
                .collect(Collectors.toList());
        }
        if (expected.equals(actual)) {
            return Collections.emptyList();
        }

        Map<String, NewsNote> expectedMap = expected.stream()
            .collect(Collectors.toMap(NewsNote::getUrl, Function.identity()));
        Map<String, NewsNote> actualMap = actual.stream()
            .collect(Collectors.toMap(NewsNote::getUrl, Function.identity()));

        return expectedMap.entrySet()
            .stream()
            .map(e -> compare(e.getValue(), actualMap.get(e.getKey())))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Mismatch compare(NewsNote expectedNote, NewsNote actualNote) {
        List<String> mismatchMessages = findMismatches(expectedNote, actualNote);
        if (mismatchMessages.isEmpty()) {
            return null;
        }
        return Mismatch.builder()
            .expected(expectedNote)
            .actual(actualNote)
            .messages(mismatchMessages)
            .build();
    }

    private List<String> findMismatches(NewsNote expectedNote, NewsNote actualNote) {
        if (actualNote == null) {
            return Arrays.asList("no actual note");
        }
        return valueAnalyzers.stream()
            .map(a -> a.analyse(expectedNote, actualNote))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
