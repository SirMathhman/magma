package magma.compile;

import magma.api.Tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public record Node(
        Optional<String> type,
        Map<String, String> strings, Map<String, List<String>> stringLists,
        Map<String, List<Node>> nodeLists
) {
    public Node() {
        this(Optional.empty(), new HashMap<>(), new HashMap<>(), new HashMap<>());
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
        nodeLists.putAll(other.nodeLists);
        return this;
    }

    public Node retype(String type) {
        return new Node(Optional.of(type), strings, stringLists, nodeLists);
    }

    public boolean is(String type) {
        return this.type.filter(value -> value.equals(type)).isPresent();
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
}