package picky.parser.service.domain;

import bepicky.service.entity.ContentTagMatchStrategy;
import bepicky.service.entity.ContentTagType;
import lombok.Data;

@Data
public class FuncContentTag {

    private ContentTagType type;
    private String value;
    private ContentTagMatchStrategy matchStrategy;
}
