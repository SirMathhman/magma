package magma;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class MapNode implements Node {
    private final Map<String, String> strings;
    private final Optional<String> type;

    public MapNode() {
        this(Optional.empty(), Collections.emptyMap());
    }

    public MapNode(Optional<String> type, Map<String, String> strings) {
        this.strings = strings;
        this.type = type;
    }

    @Override
    public Node withString(String propertyKey, String propertyValue) {
        var copy = new HashMap<>(strings);
        copy.put(propertyKey, propertyValue);
        return new MapNode(type, copy);
    }

    @Override
    public Optional<String> findString(String propertyKey) {
        return Optional.ofNullable(strings.get(propertyKey));
    }

    @Override
    public Node retype(String type) {
        return new MapNode(Optional.of(type), strings);
    }

    @Override
    public boolean is(String type) {
        return this.type.filter(inner -> inner.equals(type)).isPresent();
    }
}