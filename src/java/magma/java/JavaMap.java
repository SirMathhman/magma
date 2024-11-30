package magma.java;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.stream.HeadedStream;
import magma.api.stream.ListHead;
import magma.api.stream.Stream;

import java.util.HashMap;
import java.util.Map;

public record JavaMap<K, V>(Map<K, V> map) {
    public JavaMap() {
        this(new HashMap<>());
    }

    public JavaMap<K, V> put(K name, V type) {
        map.put(name, type);
        return this;
    }

    public Stream<Tuple<K, V>> stream() {
        return new HeadedStream<>(new ListHead<>(map.entrySet()
                .stream()
                .map(entry -> new Tuple<>(entry.getKey(), entry.getValue()))
                .toList()));
    }

    public Option<V> find(K key) {
        return map.containsKey(key)
                ? new Some<>(map.get(key))
                : new None<>();
    }

    public JavaMap<K, V> putTuple(Tuple<K, V> tuple) {
        return put(tuple.left(), tuple.right());
    }
}
