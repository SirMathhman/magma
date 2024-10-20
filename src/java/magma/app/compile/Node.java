package magma.app.compile;

import magma.api.Tuple;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Node {
    Node retype(String type);

    boolean is(String type);

    Node withString(String propertyKey, String propertyValue);

    Optional<String> findString(String propertyKey);

    Optional<Node> mapNodeList(String propertyKey, Function<List<Node>, List<Node>> mapper);

    Optional<List<Node>> findNodeList(String propertyKey);

    Node withNodeList(String propertyKey, List<Node> values);

    Node merge(Node other);

    Stream<Tuple<String, String>> streamStrings();

    Stream<Tuple<String, List<Node>>> streamNodeLists();
}
