package magma.app.compile;

import magma.api.Tuple;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record MapNode(
        Optional<String> type,
        Map<String, String> strings,

        Map<String, List<String>> stringLists, Map<String, Node> nodes, Map<String,
        List<Node>> nodeLists) implements Node {
    public MapNode() {
        this(Optional.empty(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    private static String formatLine(int depth, String key, String value) {
        return "\n" + " ".repeat(depth) + "\"" + key + "\": " + value;
    }

    private static String formatNodeList(int depth, List<Node> values) {
        return values.stream()
                .map(node -> node.format(depth + 1))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    @Override
    public Node retype(String type) {
        return new MapNode(Optional.of(type), strings, stringLists, nodes, nodeLists);
    }

    @Override
    public boolean is(String type) {
        return this.type.filter(value -> value.equals(type)).isPresent();
    }

    @Override
    public Node withString(String propertyKey, String propertyValue) {
        final var copy = new HashMap<>(strings);
        copy.put(propertyKey, propertyValue);
        return new MapNode(type, copy, stringLists, nodes, nodeLists);
    }

    @Override
    public Optional<String> findString(String propertyKey) {
        return Optional.ofNullable(strings.get(propertyKey));
    }

    @Override
    public Optional<Node> mapNodeList(String propertyKey, Function<List<Node>, List<Node>> mapper) {
        return findNodeList(propertyKey).map(mapper).map(value -> withNodeList(propertyKey, value));
    }

    @Override
    public Stream<Tuple<String, List<Node>>> streamNodeLists() {
        return nodeLists.entrySet().stream().map(pair -> new Tuple<>(pair.getKey(), pair.getValue()));
    }

    @Override
    public Node withNode(String propertyKey, Node propertyValue) {
        final var copy = new HashMap<>(nodes);
        copy.put(propertyKey, propertyValue);
        return new MapNode(type, strings, stringLists, copy, nodeLists);
    }

    @Override
    public Optional<Node> findNode(String propertyKey) {
        return Optional.ofNullable(nodes.get(propertyKey));
    }

    @Override
    public Stream<Tuple<String, Node>> streamNodes() {
        return nodes.entrySet().stream().map(entry -> new Tuple<>(entry.getKey(), entry.getValue()));
    }

    @Override
    public boolean hasNode(String node) {
        return nodes.containsKey(node);
    }

    @Override
    public Node withStringList(String propertyKey, List<String> propertyValues) {
        final var copy = new HashMap<>(stringLists);
        copy.put(propertyKey, propertyValues);
        return new MapNode(type, strings, copy, nodes, nodeLists);
    }

    @Override
    public Optional<List<String>> findStringList(String propertyKey) {
        return Optional.ofNullable(stringLists.get(propertyKey));
    }

    @Override
    public Stream<Tuple<String, String>> streamStrings() {
        return strings.entrySet().stream().map(pair -> new Tuple<>(pair.getKey(), pair.getValue()));
    }

    @Override
    public Optional<List<Node>> findNodeList(String propertyKey) {
        return Optional.ofNullable(nodeLists.get(propertyKey));
    }

    @Override
    public Node withNodeList(String propertyKey, List<Node> values) {
        final var copy = new HashMap<>(nodeLists);
        copy.put(propertyKey, values);
        return new MapNode(type, strings, stringLists, nodes, copy);
    }

    @Override
    public String toString() {
        return format(0);
    }

    @Override
    public String format(int depth) {
        final var typeString = type.map(value -> value + " ").orElse("");

        final var joinedStrings = formatMap(depth, strings, value -> "\"" + value + "\"");
        final var joinedStringLists = formatMap(depth, stringLists, this::formatStringList);
        final var joinedNodes = formatMap(depth, nodes, value -> value.format(depth + 1));
        final var joinedNodeLists = formatMap(depth, nodeLists, values -> formatNodeList(depth, values));

        final List<String> list = new ArrayList<>();

        if (!joinedStrings.isEmpty()) list.add(joinedStrings);
        if (!joinedStringLists.isEmpty()) list.add(joinedStringLists);

        if (!joinedNodes.isEmpty()) list.add(joinedNodes);
        if (!joinedNodeLists.isEmpty()) list.add(joinedNodeLists);

        final var joined = String.join(",", list);
        return typeString + "{" + joined + "\n" + " ".repeat(depth) + "}";
    }

    private String formatStringList(List<String> list) {
        return list.stream()
                .map(inner -> "\"" + inner + "\"")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private <T> String formatMap(int depth, Map<String, T> set, Function<T, String> format) {
        return set.entrySet()
                .stream()
                .map(entry -> formatLine(depth, entry.getKey(), format.apply(entry.getValue())))
                .collect(Collectors.joining(","));
    }

    @Override
    public Stream<Tuple<String, List<String>>> streamStringLists() {
        return stringLists.entrySet()
                .stream()
                .map(entry -> new Tuple<>(entry.getKey(), entry.getValue()));
    }

    @Override
    public Optional<Node> mapStringList(String propertyKey, Function<List<String>, List<String>> mapper) {
        return findStringList(propertyKey).map(mapper).map(list -> withStringList(propertyKey, list));
    }

    @Override
    public Node merge(Node other) {
        final var stringsCopy = new HashMap<>(strings);
        other.streamStrings().forEach(tuple -> stringsCopy.put(tuple.left(), tuple.right()));

        final var stringListCopy = new HashMap<>(stringLists);
        other.streamStringLists().forEach(tuple -> stringListCopy.put(tuple.left(), tuple.right()));

        final var nodesCopy = new HashMap<>(nodes);
        other.streamNodes().forEach(tuple -> nodesCopy.put(tuple.left(), tuple.right()));

        final var nodeListCopy = new HashMap<>(nodeLists);
        other.streamNodeLists().forEach(tuple -> nodeListCopy.put(tuple.left(), tuple.right()));

        return new MapNode(type, stringsCopy, stringListCopy, nodesCopy, nodeListCopy);
    }
}