package magma;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

import java.util.Collections;
import java.util.Map;

final class Node {
    private final Map<String, String> strings;

    public Node() {
        this(Collections.emptyMap());
    }

    public Node(Map<String, String> strings) {
        this.strings = strings;
    }

    public Node withString(String propertyKey, String propertyValue) {
        strings.put(propertyKey, propertyValue);
        return this;
    }

    public Option<String> find(String propertyKey) {
        return strings.containsKey(propertyKey)
                ? new Some<>(strings.get(propertyKey))
                : new None<>();
    }
}
