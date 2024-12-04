package magma.java;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public record JavaMap<K, V>(Map<K, V> strings) {
    public JavaMap() {
        this(Collections.emptyMap());
    }

    public Option<V> find(K propertyKey) {
        return strings().containsKey(propertyKey)
                ? new Some<>(strings().get(propertyKey))
                : new None<>();
    }

    public JavaMap<K, V> put(K propertyKey, V propertyValue) {
        final var copy = new HashMap<>(strings);
        copy.put(propertyKey, propertyValue);
        return new JavaMap<>(copy);
    }
}