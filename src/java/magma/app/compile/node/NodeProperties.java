package magma.app.compile.node;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.api.stream.Stream;
import magma.java.JavaSet;

import java.util.function.Function;

public interface NodeProperties<T> {
    Option<Node> with(String propertyKey, T propertyValue);

    Option<T> find(String propertyKey);

    Stream<Tuple<String, T>> stream();

    Option<Node> map(String propertyKey, Function<T, T> mapper);

    boolean has(String propertyKey);

    Option<Node> remove(String propertyKey);

    NodeProperties<T> merge(NodeProperties<T> other, MergeStrategy strategy);

    Stream<String> streamKeys();
}
