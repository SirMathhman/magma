package magma;

import java.util.Optional;

public interface Node {
    Optional<String> find(String propertyKey);

    Node withString(String propertyKey, String propertyValue);

    Node merge(Node other);
}
