package magma;

import magma.error.Display;
import magma.java.JavaList;
import magma.option.None;
import magma.option.Option;
import magma.option.Some;

import java.util.HashMap;
import java.util.Map;

public record Node(
        Option<String> type,
        Map<String, Integer> integers,
        Map<String, String> strings,
        Map<String, Node> nodes,
        Map<String, JavaList<Node>> nodeLists
) implements Display {
    public Node(String type) {
        this(new Some<>(type), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    public Node() {
        this(new None<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    public Node withInt(String propertyKey, int propertyValue) {
        integers.put(propertyKey, propertyValue);
        return this;
    }

    public Node withString(String propertyKey, String propertyValue) {
        strings.put(propertyKey, propertyValue);
        return this;
    }

    public Node withNodeList0(String propertyKey, JavaList<Node> propertyValues) {
        nodeLists.put(propertyKey, propertyValues);
        return this;
    }

    public boolean is(String type) {
        return this.type.filter(value -> value.equals(type)).isPresent();
    }

    public Option<String> findString(String propertyKey) {
        return strings.containsKey(propertyKey)
                ? new Some<>(strings.get(propertyKey))
                : new None<>();
    }

    public Option<JavaList<Node>> findNodeList(String propertyKey) {
        return nodeLists.containsKey(propertyKey)
                ? new Some<>(nodeLists.get(propertyKey))
                : new None<>();
    }

    public Option<Integer> findInt(String propertyKey) {
        return integers.containsKey(propertyKey)
                ? new Some<>(integers.get(propertyKey))
                : new None<>();
    }

    @Override
    public String display() {
        return toString();
    }

    public Node retype(String type) {
        return new Node(new Some<>(type), integers, strings, nodes, nodeLists);
    }

    public Node merge(Node other) {
        integers.putAll(other.integers);
        strings.putAll(other.strings);
        nodes.putAll(other.nodes);
        nodeLists.putAll(other.nodeLists);
        return this;
    }

    public Node withNode(String propertyKey, Node propertyValue) {
        nodes.put(propertyKey, propertyValue);
        return this;
    }

    public Option<Node> findNode(String propertyKey) {
        return nodes.containsKey(propertyKey)
                ? new Some<>(nodes.get(propertyKey))
                : new None<>();
    }
}
