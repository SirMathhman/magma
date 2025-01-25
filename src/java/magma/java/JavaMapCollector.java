package magma.java;

import magma.api.Tuple;
import magma.api.stream.Collector;

public class JavaMapCollector<K, V> implements Collector<Tuple<K, V>, JavaMap<K, V>> {
    @Override
    public JavaMap<K, V> createInitial() {
        return new JavaMap<>();
    }

    @Override
    public JavaMap<K, V> fold(JavaMap<K, V> current, Tuple<K, V> element) {
        return current.with(element.left(), element.right()).orElse(current);
    }
}
