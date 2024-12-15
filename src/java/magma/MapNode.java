package magma;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;

import java.util.HashMap;
import java.util.Map;

public record MapNode(Map<String, String> strings) {
    public MapNode() {
        this(new HashMap<>());
    }

    public MapNode withString(String propertyKey, String propertyValue) {
        strings.put(propertyKey, propertyValue);
        return this;
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