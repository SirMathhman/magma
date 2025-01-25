package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.api.stream.Stream;

import java.util.function.Function;

public interface NodeProperties<T> {
    Node with(String propertyKey, T propertyValue);

    Option<T> find(String propertyKey);

    Stream<Tuple<String, T>> stream();

    Option<Node> map(String propertyKey, Function<T, T> mapper);

    boolean has(String propertyKey);

    Option<Node> remove(String propertyKey);
}
