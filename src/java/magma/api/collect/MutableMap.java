package magma.api.collect;

import magma.api.stream.Stream;
import magma.api.stream.Streams;
import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;

import java.util.HashMap;
import java.util.Map;

public record MutableMap<K, V>(Map<K, V> map) implements magma.api.collect.Map<K, V> {
    public MutableMap() {
        this(new HashMap<>());
    }

    @Override
    public magma.api.collect.Map<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    @Override
    public Stream<Tuple<K, V>> stream() {
        return Streams.from(map.entrySet()
                .stream()
                .map(entry -> new Tuple<>(entry.getKey(), entry.getValue()))
                .toList());
    }

    @Override
    public Option<V> find(K key) {
        return map.containsKey(key)
                ? new Some<>(map.get(key))
                : new None<>();
    }
}
