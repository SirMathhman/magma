package magma;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class Node {
    public static final String VALUE = "value";
    private final Map<String, String> strings;

    public Node() {
        this(new HashMap<>());
    }

    public Node(Map<String, String> strings) {
        this.strings = strings;
    }

    public Node withString(String propertyKey, String propertyValues) {
        this.strings.put(propertyKey, propertyValues);
        return this;
    }

    public Optional<String> findString(String propertyKey) {
        return Optional.of(this.strings.get(propertyKey));
    }
}
