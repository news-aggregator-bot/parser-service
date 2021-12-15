package picky.parser.dto;

import lombok.Data;

@Data
public class TestSource {
    private long id;
    private String name;
    private String status;

    public boolean isEnabled() {
        return !"DISABLED".equals(status);
    }
}
