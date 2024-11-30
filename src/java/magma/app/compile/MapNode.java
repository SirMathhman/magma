package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.stream.Stream;
import magma.java.JavaList;
import magma.java.JavaMap;

import java.util.function.Function;

public record MapNode(
        Option<String> type,
        JavaMap<String, Integer> integers,
        JavaMap<String, String> strings,
        JavaMap<String, Node> nodes,
        JavaMap<String, JavaList<Node>> nodeLists
) implements Node {
    public MapNode(String type) {
        this(new Some<>(type), new JavaMap<>(), new JavaMap<>(), new JavaMap<>(), new JavaMap<>());
    }

    public MapNode() {
        this(new None<>(), new JavaMap<>(), new JavaMap<>(), new JavaMap<>(), new JavaMap<>());
    }

    private static <T> StringBuilder joinAt(int depth, Function<T, String> formatter, StringBuilder buffer, Tuple<String, T> entry) {
        final var values = formatter.apply(entry.right());
        return buffer.append("\n")
                .append("\t".repeat(depth + 1))
                .append(entry.left())
                .append(" = ")
                .append(values);
    }

    @Override
    public Node withInt(String propertyKey, int propertyValue) {
        return new MapNode(type, integers.put(propertyKey, propertyValue), strings, nodes, nodeLists);
    }

    @Override
    public Node withString(String propertyKey, String propertyValue) {
        return new MapNode(type, integers, strings.put(propertyKey, propertyValue), nodes, nodeLists);
    }

    @Override
    public Node withNodeList(String propertyKey, JavaList<Node> propertyValues) {
        return new MapNode(type, integers, strings, nodes, nodeLists.put(propertyKey, propertyValues));
    }

    @Override
    public String toString() {
        return format(0);
    }

    @Override
    public String format(int depth) {
        final var joinedIntegers = join(integers, depth, String::valueOf);
        final var joinedStrings = join(strings, depth, value -> "\"" + value + "\"");
        final var joinedNodes = join(nodes, depth, node -> node.format(depth + 1));
        final var joinedNodeLists = join(nodeLists, depth, nodeJavaList -> formatNodeList(nodeJavaList, depth));

        final var filter = new JavaList<String>()
                .add(joinedIntegers)
                .add(joinedStrings)
                .add(joinedNodes)
                .add(joinedNodeLists)
                .stream()
                .filter(value -> !value.isEmpty())
                .foldLeft((previous, next) -> previous + "," + next)
                .orElse("");

        return type.map(inner -> inner + " ").orElse("") + "{" +
                filter +
                "\n" + "\t".repeat(depth) + "}";
    }

    private static String formatNodeList(JavaList<Node> nodeList, int depth) {
        return "[" + nodeList
                .stream()
                .map(node -> node.format(depth + 1))
                .foldLeft((previous, next) -> previous + ", " + next)
                .orElse("") + "]";
    }

    @Override
    public <T> String join(JavaMap<String, T> map, int depth, Function<T, String> formatter) {
        return map.stream()
                .foldLeft(new StringBuilder(), (buffer, entry) -> joinAt(depth, formatter, buffer, entry))
                .toString();
    }

    @Override
    public boolean is(String type) {
        return this.type.filter(value -> value.equals(type)).isPresent();
    }

    @Override
    public Option<String> findString(String propertyKey) {
        return strings.find(propertyKey);
    }

    @Override
    public Option<JavaList<Node>> findNodeList(String propertyKey) {
        return nodeLists.find(propertyKey);
    }

    @Override
    public Option<Integer> findInt(String propertyKey) {
        return integers.find(propertyKey);
    }

    @Override
    public String display() {
        return toString();
    }

    @Override
    public Node retype(String type) {
        return new MapNode(new Some<>(type), integers, strings, nodes, nodeLists);
    }

    @Override
    public Stream<Tuple<String, JavaList<Node>>> streamNodeLists() {
        return nodeLists.stream();
    }

    @Override
    public Stream<Tuple<String, Node>> streamNodes() {
        return nodes.stream();
    }

    @Override
    public Stream<Tuple<String, String>> streamStrings() {
        return strings.stream();
    }

    @Override
    public Stream<Tuple<String, Integer>> streamIntegers() {
        return integers.stream();
    }

    @Override
    public Node merge(Node other) {
        final var integers = other.streamIntegers().foldLeft(this.integers, JavaMap::putTuple);
        final var strings = other.streamStrings().foldLeft(this.strings, JavaMap::putTuple);
        final var nodes = other.streamNodes().foldLeft(this.nodes, JavaMap::putTuple);
        final var nodeLists = other.streamNodeLists().foldLeft(this.nodeLists, JavaMap::putTuple);
        return new MapNode(type, integers, strings, nodes, nodeLists);
    }

    @Override
    public Node withNode(String propertyKey, Node propertyValue) {
        return new MapNode(type, integers, strings, nodes.put(propertyKey, propertyValue), nodeLists);
    }

    @Override
    public Option<Node> findNode(String propertyKey) {
        return nodes.find(propertyKey);
    }
}
