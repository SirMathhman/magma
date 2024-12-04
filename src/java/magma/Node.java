package magma;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Node {
    public static final String NAMESPACE_VALUE = "value";
    private final Option<String> type;
    private final Map<String, String> strings;

    public Node(Option<String> type) {
        this(type, Collections.emptyMap());
    }

    public Node(Option<String> type, Map<String, String> strings) {
        this.type = type;
        this.strings = strings;
    }

    public Node() {
        this(new None<>(), Collections.emptyMap());
    }

    public Node withString(String propertyKey, String propertyValue) {
        final var copy = new HashMap<>(strings);
        copy.put(propertyKey, propertyValue);
        return new Node(type, copy);
    }

    public Node retype(String type) {
        return new Node(new Some<>(type), strings);
    }

    public Option<String> findString(String propertyKey) {
        return strings.containsKey(propertyKey)
                ? new Some<>(strings.get(propertyKey))
                : new None<>();
    }

    public String display() {
        return toString();
    }
}