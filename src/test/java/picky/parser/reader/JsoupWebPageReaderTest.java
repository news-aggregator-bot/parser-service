package picky.parser.reader;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.*;

class JsoupWebPageReaderTest {

    private WebPageReader reader = new JsoupWebPageReader();

    @Test
    public void read_google_ShouldNotBeNull() {
        Document google = reader.read("https://www.google.com");

        assertNotNull(google);
    }

    @Test
    public void read_google_ShouldThrowMalformedUrl() {
        IllegalArgumentException iae = assertThrows(
            IllegalArgumentException.class,
            () -> reader.read("www.google.com")
        );
        assertEquals("Malformed URL: www.google.com", iae.getMessage());
    }

    @Test
    public void name_JSOUP() {
        assertEquals("JSOUP", reader.name());
    }
}