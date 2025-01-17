package magma.api.compile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class Node {
    private final Map<String, String> strings;

    private Node(Map<String, String> strings) {
        this.strings = strings;
    }

    public Node() {
        this(new HashMap<>());
    }

    public Node withString(String propertyKey, String propertyValue) {
        this.strings.put(propertyKey, propertyValue);
        return this;
    }

    public Optional<String> findString(String propertyKey) {
        return Optional.of(this.strings.get(propertyKey));
    }

    public Node merge(Node other) {
        final var copy = new HashMap<>(this.strings);
        copy.putAll(other.strings);
        return new Node(copy);
    }
}