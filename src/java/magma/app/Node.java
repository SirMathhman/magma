package magma.app;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface Node {
    Node withNodeList(String propertyKey, List<Node> propertyValues);

    Optional<List<Node>> findNodeList(String propertyKey);

    Node withString(String propertyKey, String propertyValue);

    Optional<String> findString(String propertyKey);

    Node withNode(String propertyKey, Node propertyValue);

    Optional<Node> findNode(String propertyKey);

    Node mapString(String propertyKey, Function<String, String> mapper);

    Node merge(Node other);

    String display();
}
