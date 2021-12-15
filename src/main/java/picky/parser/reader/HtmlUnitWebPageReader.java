package picky.parser.reader;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.SilentJavaScriptErrorListener;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Order(1)
public class HtmlUnitWebPageReader implements WebPageReader {

    private final WebClient client;
    private final int timeout;

    public HtmlUnitWebPageReader(@Value("${read.timeout}") int timeout) {
        client = new WebClient();
        client.setJavaScriptTimeout(timeout * 1000L);
        client.getOptions().setThrowExceptionOnScriptError(false);
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        client.getOptions().setJavaScriptEnabled(true);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setUseInsecureSSL(true);
        client.getOptions().setTimeout(timeout * 1000);
        client.getOptions().setDownloadImages(false);
        client.setJavaScriptErrorListener(new SilentJavaScriptErrorListener());
        client.setAjaxController(new NicelyResynchronizingAjaxController());
        client.waitForBackgroundJavaScriptStartingBefore(200);
        client.waitForBackgroundJavaScript(10000);
        this.timeout = timeout;
    }

    @Override
    public Document read(String path) {

        try {
            HtmlPage page;
            synchronized (this) {
                WebRequest wr = new WebRequest(new URL(path));
                wr.setCharset(StandardCharsets.UTF_8);
                wr.setTimeout(timeout * 1000);
                page = client.getPage(wr);
            }
            return Parser.parse(page.asXml(), path);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String name() {
        return "BROWSER";
    }

    @PreDestroy
    public void closeClient() {
        client.close();
    }

}