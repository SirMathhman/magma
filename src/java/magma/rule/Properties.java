package magma.rule;

import magma.Node;

import java.util.Optional;

public interface Properties<T> {
    Node with(String propertyKey, T propertyValue);

    Optional<T> find(String propertyKey);
}
