package magma;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class Node {
    private final Map<String, String> strings;

    public Node(Map<String, String> strings) {
        this.strings = strings;
    }

    public Node() {
        this(new HashMap<>());
    }

    Optional<Node> mapString(String propertyKey, Function<String, String> mapper) {
        return findString(propertyKey).map(inputContent -> {
            var outputContent = mapper.apply(inputContent);
            return withString(propertyKey, outputContent);
        });
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