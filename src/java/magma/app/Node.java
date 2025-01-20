package magma.app;

import magma.api.Tuple;
import magma.api.stream.Stream;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface Node {
    Node withNodeList(String propertyKey, List<Node> propertyValues);

    Optional<List<Node>> findNodeList(String propertyKey);

    Node withString(String propertyKey, String propertyValue);

    Optional<String> findString(String propertyKey);

    Node withNode(String propertyKey, Node propertyValue);

    String format(int depth);

    Optional<Node> findNode(String propertyKey);

    Node mapString(String propertyKey, Function<String, String> mapper);

    Node merge(Node other);

    Stream<Tuple<String, List<Node>>> streamNodeLists();

    Stream<Tuple<String, Node>> streamNodes();

    String display();

    Node retype(String type);

    boolean is(String type);

    Node mapNodeList(String propertyKey, Function<List<Node>, List<Node>> mapper);
}
