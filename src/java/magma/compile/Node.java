package magma.compile;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.java.JavaList;
import magma.java.JavaMap;

import java.util.function.Function;

public record Node(
        Option<String> type,
        JavaMap<String, String> strings,
        JavaMap<String, JavaList<String>> stringLists,
        JavaMap<String, Node> nodes, JavaMap<String, JavaList<Node>> nodeLists
) {
    public static final String NAMESPACE_VALUE = "slice";

    public Node(Option<String> type) {
        this(type, new JavaMap<>(), new JavaMap<>(), new JavaMap<>(), new JavaMap<>());
    }

    public Node() {
        this(new None<>(), new JavaMap<>(), new JavaMap<>(), new JavaMap<>(), new JavaMap<>());
    }

    @Override
    public String toString() {
        return "Node{" +
                "type=" + type +
                ", strings=" + strings +
                ", nodeLists=" + nodeLists +
                ", stringLists=" + stringLists +
                '}';
    }

    public Node withString(String propertyKey, String propertyValue) {
        return new Node(type, strings.put(propertyKey, propertyValue), stringLists, nodes, nodeLists);
    }

    public Node retype(String type) {
        return new Node(new Some<>(type), strings, stringLists, nodes, nodeLists);
    }

    public Option<String> findString(String propertyKey) {
        return strings.find(propertyKey);
    }

    public String display() {
        return toString();
    }

    public Node withNodeList(String propertyKey, JavaList<Node> propertyValues) {
        return new Node(type, strings, stringLists, nodes, nodeLists.put(propertyKey, propertyValues));
    }

    public Option<JavaList<Node>> findNodeList(String propertyKey) {
        return nodeLists.find(propertyKey);
    }

    public Option<Node> mapNodeList(String propertyKey, Function<JavaList<Node>, JavaList<Node>> mapper) {
        return findNodeList(propertyKey).map(mapper).map(list -> withNodeList(propertyKey, list));
    }

    public boolean is(String type) {
        return this.type.filter(inner -> inner.equals(type)).isPresent();
    }

    public Node merge(Node other) {
        return new Node(type,
                strings.putAll(other.strings),
                stringLists.putAll(other.stringLists),
                nodes.putAll(other.nodes),
                nodeLists.putAll(other.nodeLists)
        );
    }

    public Node withStringList(String propertyKey, JavaList<String> propertyValues) {
        return new Node(type, strings, stringLists.put(propertyKey, propertyValues), nodes, nodeLists);
    }

    public Option<JavaList<String>> findStringList(String propertyKey) {
        return stringLists.find(propertyKey);
    }

    public Node withNode(String propertyKey, Node propertyValue) {
        return new Node(type, strings, stringLists, nodes.put(propertyKey, propertyValue), nodeLists);
    }

    public Option<Node> findNode(String propertyKey) {
        return nodes.find(propertyKey);
    }
}