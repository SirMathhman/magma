package magma;

import java.util.Optional;

public interface Node {
    Node withString(String propertyKey, String propertyValues);

    Optional<String> findString(String propertyKey);
}
