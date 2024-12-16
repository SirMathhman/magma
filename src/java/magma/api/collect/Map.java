package magma.api.collect;

import magma.api.stream.Stream;
import magma.api.Tuple;
import magma.api.option.Option;

public interface Map<K, V> {
    Map<K, V> put(K key, V value);

    Stream<Tuple<K, V>> stream();

    Option<V> find(K key);
}
