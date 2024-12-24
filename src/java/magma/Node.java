package magma;

import java.util.*;

public record Node(Map<String, List<String>> stringLists) {
    public Node() {
        this(new HashMap<>());
    }

    public Optional<List<String>> findStringList(String propertyKey) {
        return Optional.of(stringLists.get(propertyKey));
    }

    public Node withStringList(String propertyKey, List<String> propertyValues) {
        stringLists.put(propertyKey, propertyValues);
        return this;
    }
}