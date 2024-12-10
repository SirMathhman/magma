package magma;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record Node(Map<String, String> strings) {
    public Node() {
        this(new HashMap<>());
    }

    public Node withString(String propertyKey, String propertyValue) {
        strings.put(propertyKey, propertyValue);
        return this;
    }

    public Optional<String> findString(String propertyKey) {
        return Optional.ofNullable(strings.get(propertyKey));
    }

    public Node merge(Node other) {
        strings.putAll(other.strings);
        return this;
    }
}
