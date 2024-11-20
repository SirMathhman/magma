package magma;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record JavaMap<K, V>(Map<K, V> map) {
    public JavaMap() {
        this(Collections.emptyMap());
    }

    Optional<V> find(K propertyKey) {
        return Optional.ofNullable(map().get(propertyKey));
    }

    JavaMap<K, V> put(K key, V value) {
        final var copy = new HashMap<>(map());
        copy.put(key, value);
        return new JavaMap<>(copy);
    }
}