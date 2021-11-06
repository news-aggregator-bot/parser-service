package picky.parser.reader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HtmlUnitWebPageReaderTest {

    private WebPageReader reader = new HtmlUnitWebPageReader();

    @Test
    public void name_BROWSER() {
        assertEquals("BROWSER", reader.name());
    }
}