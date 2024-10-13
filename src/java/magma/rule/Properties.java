package magma.rule;

import magma.Node;

import java.util.Optional;
import java.util.function.Function;

public interface Properties<T> {
    Node with(String propertyKey, T propertyValue);

    Optional<T> find(String propertyKey);

    Optional<Node> map(String propertyKey, Function<T, T> mapper);
}
