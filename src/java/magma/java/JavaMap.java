package magma.java;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.stream.Stream;
import magma.api.stream.head.HeadedStream;
import magma.api.stream.head.ListHead;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public record JavaMap<K, V>(Map<K, V> map) {
    public JavaMap() {
        this(Collections.emptyMap());
    }

    public Option<V> find(K propertyKey) {
        return map().containsKey(propertyKey)
                ? new Some<>(map().get(propertyKey))
                : new None<>();
    }

    public JavaMap<K, V> put(K propertyKey, V propertyValue) {
        final var copy = new HashMap<>(map);
        copy.put(propertyKey, propertyValue);
        return new JavaMap<>(copy);
    }

    public JavaMap<K, V> putAll(JavaMap<K, V> other) {
        final var copy = new HashMap<>(map);
        copy.putAll(other.map);
        return new JavaMap<>(copy);
    }

    public Stream<Tuple<K, V>> stream() {
        return new HeadedStream<>(new ListHead<>(map.entrySet()
                .stream()
                .map(entry -> new Tuple<>(entry.getKey(), entry.getValue()))
                .toList()));
    }
}