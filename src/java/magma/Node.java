package magma;

import java.util.List;
import java.util.Optional;

public interface Node {
    Node withNodeList(String propertyKey, List<Node> propertyValues);

    Optional<List<Node>> findNodeList(String propertyKey);

    Node withString(String propertyKey, String propertyValue);

    Optional<String> findString(String propertyKey);

    Node withNode(String propertyKey, Node propertyValue);

    Optional<Node> findNode(String propertyKey);
}
