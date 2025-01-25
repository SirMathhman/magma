package magma.java;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.stream.Stream;
import magma.api.stream.Streams;

import java.util.HashMap;
import java.util.Map;

public record JavaMap<K, V>(Map<K, V> map) {
    public JavaMap() {
        this(new HashMap<>());
    }

    public Option<JavaMap<K, V>> with(K key, V value) {
        if (this.map.containsKey(key)) return new None<>();

        final var copy = new HashMap<>(this.map);
        copy.put(key, value);
        return new Some<>(new JavaMap<>(copy));
    }

    public Option<V> find(K propertyKey) {
        return this.map.containsKey(propertyKey)
                ? new Some<>(this.map.get(propertyKey))
                : new None<>();
    }

    public Stream<Tuple<K, V>> stream() {
        return Streams.fromNativeList(this.map.entrySet()
                .stream()
                .map(entry -> new Tuple<>(entry.getKey(), entry.getValue()))
                .toList());
    }

    public boolean has(K propertyKey) {
        return this.map.containsKey(propertyKey);
    }

    public Option<JavaMap<K, V>> remove(K key) {
        if (!this.map.containsKey(key)) return new None<>();

        final var copy = new HashMap<>(this.map);
        copy.remove(key);
        return new Some<>(new JavaMap<>(copy));
    }

    public Stream<K> streamKeys() {
        return Streams.fromNativeSet(this.map.keySet());
    }
}
