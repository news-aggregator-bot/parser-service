package picky.parser.service.domain;

import lombok.Data;

import java.util.List;

@Data
public class FuncSource {

    private String name;
    private List<FuncSourcePage> sourcePages;
}
