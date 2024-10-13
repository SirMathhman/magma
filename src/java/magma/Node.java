package magma;

import java.util.Optional;

public interface Node {
    Node withString(String propertyKey, String propertyValue);

    Optional<String> findString(String propertyKey);

    Node retype(String type);

    boolean is(String type);
}
