package magma;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record Node(Map<String, List<String>> stringLists) {
    public Node() {
        this(Collections.emptyMap());
    }

    public Optional<List<String>> findStringList(String propertyKey) {
        return Optional.of(stringLists.get(propertyKey));
    }

    public Node withStringList(String propertyKey, List<String> propertyValues) {
        stringLists.put(propertyKey, propertyValues);
        return this;
    }
}