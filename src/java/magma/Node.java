package magma;

import magma.error.CompileError;
import magma.option.Option;
import magma.result.Result;

public interface Node {
    Node retype(String type);

    boolean is(String type);

    String display();

    Node merge(Node node);

    MapNode withNodeList(String propertyKey, List<Node> propertyValues);

    Stream<Tuple<String, String>> streamStrings();

    Node withString(String propertyKey, String propertyValue);

    Stream<Tuple<String, List<Node>>> streamNodeLists();

    Option<List<Node>> findNodeList(String propertyKey);

    Option<String> findString(String propertyKey);
}
