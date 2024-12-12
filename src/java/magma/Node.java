package magma;

import java.util.Optional;

public interface Node {
    Optional<String> find(String propertyKey);
}
