package picky.parser.service;

import picky.parser.dto.ParsedNews;
import picky.parser.dto.SourcePage;

public interface WebContentParser {

    ParsedNews parse(SourcePage page);
}
