package magma;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class Node {
    private final Optional<String> type;
    private final Map<String, String> strings;

    private Node(Optional<String> type, Map<String, String> strings) {
        this.type = type;
        this.strings = strings;
    }

    public Node(String type) {
        this(Optional.of(type));
    }

    public Node(Optional<String> type) {
        this(type, Collections.emptyMap());
    }

    public Node withString(String propertyKey, String propertyValue) {
        final var copy = new HashMap<>(strings);
        copy.put(propertyKey, propertyValue);
        return new Node(type, copy);
    }

    public Node retype(String type) {
        return new Node(Optional.of(type), strings);
    }

    public boolean is(String type) {
        return this.type.isPresent() && this.type.get().equals(type);
    }

    public Optional<String> findValue(String propertyKey) {
        return Optional.of(strings.get(propertyKey));
    }
}
