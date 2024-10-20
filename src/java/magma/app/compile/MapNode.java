package magma.app.compile;

import magma.api.Tuple;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record MapNode(Optional<String> type, Map<String, String> strings,
                      Map<String, List<Node>> nodeLists) implements Node {
    public MapNode() {
        this(Optional.empty(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Override
    public Node retype(String type) {
        return new MapNode(Optional.of(type), strings, nodeLists);
    }

    @Override
    public boolean is(String type) {
        return this.type.filter(value -> value.equals(type)).isPresent();
    }

    @Override
    public Node withString(String propertyKey, String propertyValue) {
        final var copy = new HashMap<>(strings);
        copy.put(propertyKey, propertyValue);
        return new MapNode(type, copy, nodeLists);
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
        return new MapNode(type, strings, copy);
    }

    @Override
    public String toString() {
        return format(0);
    }

    @Override
    public String format(int depth) {
        final var typeString = type.map(value -> value + " ").orElse("");
        final var joinedStrings = strings.entrySet()
                .stream()
                .map(entry -> "\n" + " ".repeat(depth) + "\"" + entry.getKey() + "\": \"" + entry.getValue() + "\"")
                .collect(Collectors.joining(","));
        final var joinedNodeLists = nodeLists.entrySet()
                .stream()
                .map(entry -> {
                    final var value = entry.getValue().stream()
                            .map(node -> node.format(depth + 1))
                            .collect(Collectors.joining(", ", "[", "]"));

                    final var s = " ".repeat(depth);
                    return "\n" + s + "\"" + entry.getKey() + "\": \"" + value + "\"";
                })
                .collect(Collectors.joining());

        final List<String> list = new ArrayList<>();
        if(!joinedStrings.isEmpty()) list.add(joinedStrings);
        if(!joinedNodeLists.isEmpty()) list.add(joinedNodeLists);

        final var joined = String.join(",", list);
        return typeString + "{" + joined + "\n" + " ".repeat(depth) + "}";
    }

    @Override
    public Node merge(Node other) {
        final var stringsCopy = new HashMap<>(strings);
        other.streamStrings().forEach(tuple -> stringsCopy.put(tuple.left(), tuple.right()));

        final var nodeListCopy = new HashMap<>(nodeLists);
        other.streamNodeLists().forEach(tuple -> nodeListCopy.put(tuple.left(), tuple.right()));

        return new MapNode(type, stringsCopy, nodeListCopy);
    }
}
