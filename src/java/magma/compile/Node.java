package magma.compile;

import magma.api.Tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record Node(
        Optional<String> maybeType,
        Map<String, String> strings,
        Map<String, List<String>> stringLists,
        Map<String, Node> nodes,
        Map<String, List<Node>> nodeLists
) {
    public Node() {
        this(Optional.empty(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    public Node(String type) {
        this(Optional.of(type), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    private static String formatString(String value) {
        return "\"" + value.replaceAll("\\n", "\\\\n") + "\"";
    }

    @Override
    public String toString() {
        return format(0);
    }

    private String format(int depth) {
        final var typeString = maybeType.map(type -> type + " ").orElse("");
        final var strings = formatMap(this.strings, Node::formatString, depth);
        final var stringLists = formatMap(this.stringLists, strings1 -> strings1.stream()
                .map(Node::formatString)
                .collect(Collectors.joining(", ", "[", "]")), depth);
        final var nodes = formatMap(this.nodes, node -> node.format(depth + 1), depth);
        final var nodeLists = formatMap(this.nodeLists, nodeList -> nodeList.stream()
                .map(node -> node.format(depth + 1))
                .collect(Collectors.joining(", ", "[", "]")), depth);

        final var values = Stream.of(strings, stringLists, nodes, nodeLists)
                .flatMap(Optional::stream)
                .collect(Collectors.joining(", "));

        final var after = "\t".repeat(Math.max(depth, 0));
        return typeString + "{" + values + "\n" + after + "}";
    }

    private <T> Optional<String> formatMap(Map<String, T> map, Function<T, String> mapper, int depth) {
        if (map.isEmpty()) return Optional.empty();

        var stringsBuilder = new StringJoiner(", ");
        for (Map.Entry<String, T> entry : map.entrySet()) {
            stringsBuilder.add(new StringBuilder()
                    .append("\n")
                    .append("\t".repeat(depth + 1))
                    .append("\"")
                    .append(entry.getKey())
                    .append("\": ")
                    .append(mapper.apply(entry.getValue())));
        }
        return Optional.of(stringsBuilder.toString());
    }

    public Optional<List<String>> findStringList(String propertyKey) {
        return Optional.ofNullable(stringLists.get(propertyKey));
    }

    public Node withStringList(String propertyKey, List<String> propertyValues) {
        stringLists.put(propertyKey, propertyValues);
        return this;
    }

    public Node merge(Node other) {
        strings.putAll(other.strings);
        stringLists.putAll(other.stringLists);
        nodes.putAll(other.nodes);
        nodeLists.putAll(other.nodeLists);
        return this;
    }

    public Node retype(String type) {
        return new Node(Optional.of(type), strings, stringLists, nodes, nodeLists);
    }

    public boolean is(String type) {
        return this.maybeType.filter(value -> value.equals(type)).isPresent();
    }

    public Node withNodeList(String propertyKey, List<Node> propertyValues) {
        nodeLists.put(propertyKey, propertyValues);
        return this;
    }

    public Optional<List<Node>> findNodeList(String propertyKey) {
        return Optional.ofNullable(nodeLists.get(propertyKey));
    }

    public Node withString(String propertyKey, String propertyValue) {
        strings.put(propertyKey, propertyValue);
        return this;
    }

    public Optional<String> findString(String propertyKey) {
        return Optional.ofNullable(strings.get(propertyKey));
    }

    public Stream<Tuple<String, List<Node>>> streamNodeLists() {
        return nodeLists.entrySet()
                .stream()
                .map(list -> new Tuple<>(list.getKey(), list.getValue()));
    }

    public Node withNode(String propertyKey, Node propertyValue) {
        nodes.put(propertyKey, propertyValue);
        return this;
    }

    public Optional<Node> findNode(String propertyKey) {
        return Optional.ofNullable(nodes.get(propertyKey));
    }

    public Stream<Tuple<String, Node>> streamNodes() {
        return nodes.entrySet()
                .stream()
                .map(entry -> new Tuple<>(entry.getKey(), entry.getValue()));
    }
}