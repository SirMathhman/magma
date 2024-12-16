package magma;

import java.util.HashMap;
import java.util.Map;

public record MutableMap<K, V>(Map<K, V> map) implements magma.Map<K, V> {
    public MutableMap() {
        this(new HashMap<>());
    }

    @Override
    public magma.Map<K, V> put(K key, V value) {
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
}
