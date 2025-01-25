package magma.app.compile;

import magma.api.Tuple;
import magma.api.stream.Stream;
import magma.api.stream.Streams;
import magma.java.JavaList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public final class MapNode implements Node {
    private final Map<String, String> strings;
    private final Map<String, List<Node>> nodeLists;
    private final Map<String, Node> nodes;
    private final Optional<String> type;

    public MapNode() {
        this(Optional.empty(), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    public MapNode(Optional<String> type, Map<String, String> strings, Map<String, Node> nodes, Map<String, List<Node>> nodeLists) {
        this.type = type;
        this.strings = strings;
        this.nodes = nodes;
        this.nodeLists = nodeLists;
    }

    public MapNode(String type) {
        this(Optional.of(type), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    private static StringBuilder createEntry(String name, String content, int depth) {
        return new StringBuilder()
                .append("\n" + "\t".repeat(depth))
                .append(name)
                .append(" : ")
                .append(content);
    }

    @Override
    public String toString() {
        return format(0);
    }

    @Override
    public String format(int depth) {
        final var typeString = this.type.map(inner -> inner + " ").orElse("");

        var builder = new StringBuilder()
                .append(typeString)
                .append("{");

        final var joiner = new StringJoiner(",");
        this.strings.entrySet()
                .stream()
                .map(entry -> createEntry(entry.getKey(), "\"" + entry.getValue() + "\"", depth + 1))
                .forEach(joiner::add);

        this.nodes.entrySet()
                .stream()
                .map(entry -> createEntry(entry.getKey(), entry.getValue().format(depth + 1), depth + 1))
                .forEach(joiner::add);

        this.nodeLists.entrySet()
                .stream()
                .map(entry -> createEntry(entry.getKey(), entry.getValue()
                        .stream()
                        .map(node -> node.format(depth + 1))
                        .collect(Collectors.joining(",\n", "[", "]")), depth + 1))
                .forEach(joiner::add);

        builder.append(joiner);
        return builder.append("\n").append("\t".repeat(depth)).append("}").toString();
    }

    @Override
    public Node merge(Node other) {
        final var withStrings = stream(this.strings).foldLeft(other, (node, tuple) -> {
            String propertyKey = tuple.left();
            return node.inputs().with(propertyKey, new StringInput(propertyKey));
        });
        final var withNodes = nodes().stream().foldLeft(withStrings, (node, tuple) -> node.nodes().with(tuple.left(), tuple.right()));
        return nodeLists().stream().map(tuple1 -> new Tuple<>(tuple1.left(), tuple1.right().unwrap())).foldLeft(withNodes, (node, tuple) -> node.nodeLists().with(tuple.left(), new JavaList<>(tuple.right())));
    }

    private <K, V> Stream<Tuple<K, V>> stream(Map<K, V> map) {
        return Streams.from(map.entrySet()).map(entry -> new Tuple<>(entry.getKey(), entry.getValue()));
    }

    @Override
    public String display() {
        return toString();
    }

    @Override
    public Node retype(String type) {
        return new MapNode(Optional.of(type), this.strings, this.nodes, this.nodeLists);
    }

    @Override
    public boolean is(String type) {
        return this.type.isPresent() && this.type.get().equals(type);
    }

    @Override
    public boolean hasType() {
        return this.type.isPresent();
    }

    @Override
    public NodeProperties<Input> inputs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeProperties<Node> nodes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeProperties<JavaList<Node>> nodeLists() {
        throw new UnsupportedOperationException();
    }
}
