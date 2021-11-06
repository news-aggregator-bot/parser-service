package picky.parser.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode
@ToString(includeFieldNames = false)
public class ContentBlock {

    @ToString.Exclude
    private SourcePage sourcePage;

    private Set<ContentTag> tags;

    @EqualsAndHashCode.Exclude
    private Map<ContentTag.Type, ContentTag> typeMap;

    public ContentTag findByType(ContentTag.Type type) {
        if (typeMap == null) {
            typeMap = tags.stream()
                .collect(Collectors.toMap(ContentTag::getType, Function.identity()));
        }
        return typeMap.get(type);
    }
}
