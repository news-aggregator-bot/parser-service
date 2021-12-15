package picky.parser.service;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import picky.parser.dto.SourcePage;
import picky.parser.dto.UrlNormalisation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.function.BiFunction;

import static picky.parser.dto.UrlNormalisation.ASIS;
import static picky.parser.dto.UrlNormalisation.NO_PARAMS;

@Component
public class UrlNormalisationService {

    private final Map<UrlNormalisation, BiFunction<String, Element, String>> urlNormalisationContext =
        ImmutableMap.<UrlNormalisation, BiFunction<String, Element, String>>builder()
            .put(ASIS, (u, a) -> normaliseHref(u, getHref(a)))
            .put(NO_PARAMS, (u, a) -> normaliseHref(u, cutHref(a)))
            .build();

    public String normaliseUrl(SourcePage sourcePage, Element a) {
        return urlNormalisationContext.get(sourcePage.getUrlNormalisation()).apply(sourcePage.getUrl(), a);
    }

    private String getHref(Element a) {
        return a.attr("href");
    }

    private String cutHref(Element a) {
        String href = getHref(a);
        if (StringUtils.isNotBlank(href) && href.contains("?")) {
            return new StringBuilder(href).delete(href.indexOf("?"), href.length()).toString();
        }
        return href;
    }

    private String normaliseHref(String pageUrl, String href) {
        if (StringUtils.isBlank(href) || href.startsWith("http")) {
            return href;
        }
        try {
            URL url = new URL(pageUrl);
            return pageUrl.replace(url.getPath(), href);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
