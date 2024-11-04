package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.Option;

import java.util.List;
import java.util.stream.Stream;

public interface Node {
    Option<String> findString(String propertyKey);

    String asString();

    String format(int depth);

    Option<List<Node>> findNodeList(String propertyKey);

    Option<Node> withNodeList(String propertyKey, List<Node> propertyValues);

    Option<Node> withString(String propertyKey, String propertyValue);

    Option<Node> retype(String type);

    boolean is(String type);

    Option<String> findType();

    Option<Node> merge(Node other);

    boolean isTyped();

    Stream<Tuple<String, String>> streamStrings();

    Stream<Tuple<String, List<Node>>> streamNodeLists();

    Option<Node> withNode(String propertyKey, Node propertyValue);

    Option<Node> findNode(String propertyKey);

    Stream<Tuple<String, Node>> streamNodes();
}
