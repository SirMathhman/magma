package magma;

import java.util.List;
import java.util.Optional;

public interface Node {
    Node withStringList(String propertyKey, List<String> propertyValues);

    Optional<List<String>> findStringList(String propertyKey);
}
