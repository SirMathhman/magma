package magma.java;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.stream.Stream;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public record JavaMap<K, V>(Map<K, V> map) {
    public JavaMap() {
        this(Collections.emptyMap());
    }

    public Option<V> find(K key) {
        if (map().containsKey(key)) {
            return new Some<>(map().get(key));
        } else {
            return new None<>();
        }
    }

    public int size() {
        return map.size();
    }

    public JavaMap<K, V> set(K key, V value) {
        final var copy = new HashMap<>(map);
        copy.put(key, value);
        return new JavaMap<>(copy);
    }

    public Stream<Tuple<K, V>> stream() {
        return JavaStreams.fromList(map.entrySet()
                .stream()
                .map(entry -> new Tuple<>(entry.getKey(), entry.getValue()))
                .toList());
    }
}