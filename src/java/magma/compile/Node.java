package magma.compile;

import magma.java.JavaList;
import magma.java.JavaMap;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;

import java.util.function.Function;

public final class Node {
    public static final String NAMESPACE_VALUE = "slice";
    private final Option<String> type;
    private final JavaMap<String, String> strings;
    private final JavaMap<String, JavaList<Node>> nodeLists;

    public Node(Option<String> type) {
        this(type, new JavaMap<>(), new JavaMap<>());
    }

    public Node(Option<String> type, JavaMap<String, String> strings, JavaMap<String, JavaList<Node>> nodeLists) {
        this.type = type;
        this.strings = strings;
        this.nodeLists = nodeLists;
    }

    public Node() {
        this(new None<>(), new JavaMap<>(), new JavaMap<>());
    }

    public Node withString(String propertyKey, String propertyValue) {
        return new Node(type, strings.put(propertyKey, propertyValue), nodeLists);
    }

    public Node retype(String type) {
        return new Node(new Some<>(type), strings, nodeLists);
    }

    public Option<String> findString(String propertyKey) {
        return strings.find(propertyKey);
    }

    public String display() {
        return toString();
    }

    public Node withNodeList(String propertyKey, JavaList<Node> propertyValues) {
        return new Node(type, strings, nodeLists.put(propertyKey, propertyValues));
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
        return new Node(type, strings.putAll(other.strings), nodeLists.putAll(other.nodeLists));
    }
}