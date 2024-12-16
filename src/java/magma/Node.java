package magma;

import magma.option.Option;

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
}
