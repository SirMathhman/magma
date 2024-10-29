package magma.java;

import magma.core.option.None;
import magma.core.option.Option;
import magma.core.option.Some;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public record JavaMap<K, V>(Map<K, V> hidden) {
    public JavaMap() {
        this(Collections.emptyMap());
    }

    public Option<JavaMap<K, V>> put(K key, V value) {
        if (hidden.containsKey(key)) return new None<>();

        final var copy = new HashMap<>(hidden);
        copy.put(key, value);
        return new Some<>(new JavaMap<>(copy));
    }

    public Option<V> find(K key) {
        if (hidden.containsKey(key)) return new Some<>(hidden.get(key));
        else return new None<>();
    }
}
