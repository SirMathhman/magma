package magma;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class MapNode implements Node {
    private final Map<String, String> strings;

    public MapNode() {
        this(Collections.emptyMap());
    }

    public MapNode(Map<String, String> strings) {
        this.strings = strings;
    }

    @Override
    public Node withString(String propertyKey, String propertyValue) {
        var copy = new HashMap<>(strings);
        copy.put(propertyKey, propertyValue);
        return new MapNode(copy);
    }

    @Override
    public Optional<String> findString(String propertyKey) {
        return Optional.ofNullable(strings.get(propertyKey));
    }
}