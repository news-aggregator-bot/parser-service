package picky.parser.service.domain;

import lombok.Data;

import java.util.List;

@Data
public class FuncContentBlock {

    private List<FuncContentTag> contentTags;
}
