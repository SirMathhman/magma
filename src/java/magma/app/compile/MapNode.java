package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.java.JavaList;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record MapNode(
        Option<String> type,
        Map<String, String> strings,
        Map<String, Node> nodes,
        Map<String, List<Node>> nodeLists,
        Map<String, Integer> integers) implements Node {
    public MapNode(String type) {
        this(new Some<>(type), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    public MapNode() {
        this(new None<>(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Override
    public Option<String> findString(String propertyKey) {
        return strings.containsKey(propertyKey)
                ? new Some<>(strings.get(propertyKey))
                : new None<>();
    }

    @Override
    public String asString() {
        return format(0);
    }

    @Override
    public String format(int depth) {
        final var indent = "\n" + "\t".repeat(depth + 1);

        var integers = new StringJoiner(",");
        for (Map.Entry<String, Integer> entry : this.integers.entrySet()) {
            integers.add(indent + entry.getKey() + ": " + entry.getValue());
        }

        var strings = new StringJoiner(",");
        for (Map.Entry<String, String> entry : this.strings.entrySet()) {
            strings.add(indent + entry.getKey() + ": \"" + entry.getValue() + "\"");
        }

        var nodesJoiner = new StringJoiner(",");
        for (Map.Entry<String, Node> entry : this.nodes.entrySet()) {
            nodesJoiner.add(indent + entry.getKey() + ": " + entry.getValue().format(depth + 1));
        }

        var nodeListsJoiner = new StringJoiner("");
        for (Map.Entry<String, List<Node>> entry : nodeLists.entrySet()) {
            nodeListsJoiner.add(indent + entry.getKey() + ": " + entry.getValue()
                    .stream()
                    .map(value -> value.format(depth + 1))
                    .collect(Collectors.joining(", ", "[", "]")));
        }

        final var typePrefix = type.map(value -> value + " ").orElse("");
        final var joined = Stream.of(integers, strings, nodesJoiner, nodeListsJoiner)
                .map(StringJoiner::toString)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.joining(", "));

        return typePrefix + "{" + joined + "\n" + "\t".repeat(depth) + "}";
    }

    private Option<List<Node>> findNodeList0(String propertyKey) {
        return nodeLists.containsKey(propertyKey)
                ? new Some<>(nodeLists.get(propertyKey))
                : new None<>();
    }

    @Override
    public Node withNodeList(String propertyKey, List<Node> propertyValues) {
        final var copy = new HashMap<>(nodeLists);
        copy.put(propertyKey, propertyValues);
        return new MapNode(type, strings, nodes, copy, integers);
    }

    @Override
    public Node withString(String propertyKey, String propertyValue) {
        final var copy = new HashMap<>(strings);
        copy.put(propertyKey, propertyValue);
        return new MapNode(type, copy, nodes, nodeLists, integers);
    }

    @Override
    public Node retype(String type) {
        return new MapNode(new Some<>(type), strings, nodes, nodeLists, integers);
    }

    @Override
    public boolean is(String type) {
        return this.type
                .map(value -> value.equals(type))
                .orElse(false);
    }

    @Override
    public boolean isTyped() {
        return this.type.isPresent();
    }

    @Override
    public Stream<Tuple<String, String>> streamStrings() {
        return strings.entrySet()
                .stream()
                .map(pair -> new Tuple<>(pair.getKey(), pair.getValue()));
    }

    @Override
    public Stream<Tuple<String, List<Node>>> streamNodeListsToNativeStream() {
        return nodeLists.entrySet()
                .stream()
                .map(pair -> new Tuple<>(pair.getKey(), pair.getValue()));
    }

    @Override
    public Node withNode(String propertyKey, Node propertyValue) {
        final var copy = new HashMap<>(nodes);
        copy.put(propertyKey, propertyValue);
        return new MapNode(type, strings, copy, nodeLists, integers);
    }

    @Override
    public Option<Node> findNode(String propertyKey) {
        return nodes.containsKey(propertyKey)
                ? new Some<>(nodes.get(propertyKey))
                : new None<>();
    }

    @Override
    public Stream<Tuple<String, Node>> streamNodesToNativeStream() {
        return nodes.entrySet()
                .stream()
                .map(pair -> new Tuple<>(pair.getKey(), pair.getValue()));
    }

    @Override
    public Option<Result<Node, CompileError>> mapNodeList(String propertyKey, Function<List<Node>, Result<List<Node>, CompileError>> mapper) {
        return findNodeList(propertyKey).map(JavaList::list)
                .map(mapper)
                .map(result -> result.mapValue(list -> withNodeList(propertyKey, list)));
    }


    @Override
    public Option<Result<Node, CompileError>> mapNode(String propertyKey, Function<Node, Result<Node, CompileError>> mapper) {
        return findNode(propertyKey)
                .map(mapper)
                .map(result -> result.mapValue(node -> withNode(propertyKey, node)));
    }

    @Override
    public Option<Result<Node, CompileError>> mapString(String propertyKey, Function<String, Result<String, CompileError>> mapper) {
        return findString(propertyKey)
                .map(mapper)
                .map(result -> result.mapValue(string -> withString(propertyKey, string)));
    }

    @Override
    public boolean hasString(String propertyKey) {
        return strings.containsKey(propertyKey);
    }

    @Override
    public Node withInt(String propertyKey, int propertyValue) {
        final var copy = new HashMap<>(integers);
        copy.put(propertyKey, propertyValue);
        return new MapNode(type, strings, nodes, nodeLists, copy);
    }

    @Override
    public Option<Integer> findInt(String propertyKey) {
        return integers.containsKey(propertyKey)
                ? new Some<>(integers.get(propertyKey))
                : new None<>();
    }

    @Override
    public boolean hasInteger(String propertyKey) {
        return integers.containsKey(propertyKey);
    }

    @Override
    public Option<String> findType() {
        return type;
    }

    @Override
    public Option<Node> merge(Node other) {
        if (this.type.isPresent() && other.isTyped()) return new None<>();
        final var newType = this.type.or(other::findType);

        // TODO: fix bug where same key could be applied to two values
        // data erasure
        final var stringsCopy = new HashMap<>(strings);
        other.streamStrings().forEach(tuple -> stringsCopy.put(tuple.left(), tuple.right()));

        final var nodesCopy = new HashMap<>(nodes);
        other.streamNodesToNativeStream().forEach(tuple -> nodesCopy.put(tuple.left(), tuple.right()));

        final var nodeListsCopy = new HashMap<>(nodeLists);
        other.streamNodeListsToNativeStream().forEach(tuple -> nodeListsCopy.put(tuple.left(), tuple.right()));

        return new Some<>(new MapNode(newType, stringsCopy, nodesCopy, nodeListsCopy, integers));
    }

    @Override
    public Option<JavaList<Node>> findNodeList(String propertyKey) {
        return findNodeList0(propertyKey).map(JavaList::new);
    }
}