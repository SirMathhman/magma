package magma.java;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;

import java.util.Map;

public record JavaMap<K, V>(Map<K, V> map) {
    public Option<V> find(K key) {
        if (map().containsKey(key)) {
            return new Some<>(map().get(key));
        } else {
            return new None<>();
        }
    }
}