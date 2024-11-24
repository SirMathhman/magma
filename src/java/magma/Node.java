package magma;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record Node(Map<String, String> strings, Map<String, List<String>> stringLists) {
    public Node() {
        this(Collections.emptyMap(), Collections.emptyMap());
    }

    public Node withStringList(String propertyKey, List<String> propertyValues) {
        final var copy = new HashMap<>(stringLists);
        copy.put(propertyKey, propertyValues);
        return new Node(strings, copy);
    }

    public Node withString(String propertyKey, String propertyValue) {
        final var copy = new HashMap<>(strings);
        copy.put(propertyKey, propertyValue);
        return new Node(copy, stringLists);
    }

    public Option<Node> mapStringList(String propertyKey, Function<List<String>, List<String>> mapper) {
        if (!stringLists.containsKey(propertyKey)) return new None<>();

        final var mapped = mapper.apply(stringLists.get(propertyKey));
        return new Some<>(withStringList(propertyKey, mapped));
    }

    public Option<List<String>> findStringList(String propertyKey) {
        return stringLists.containsKey(propertyKey)
                ? new Some<>(stringLists.get(propertyKey))
                : new None<>();
    }

    public Option<String> findString(String propertyKey) {
        return strings.containsKey(propertyKey)
                ? new Some<>(strings.get(propertyKey))
                : new None<>();
    }

    public String display() {
        throw new UnsupportedOperationException();
    }
}
