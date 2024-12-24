package magma;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record Node(
        Optional<String> type,
        Map<String, List<String>> stringLists,
        Map<String, List<Node>> nodeLists
) {
    public Node() {
        this(Optional.empty(), new HashMap<>(), new HashMap<>());
    }

    public Optional<List<String>> findStringList(String propertyKey) {
        return Optional.ofNullable(stringLists.get(propertyKey));
    }

    public Node withStringList(String propertyKey, List<String> propertyValues) {
        stringLists.put(propertyKey, propertyValues);
        return this;
    }

    public Node merge(Node other) {
        stringLists.putAll(other.stringLists);
        return this;
    }

    public Node retype(String type) {
        return new Node(Optional.of(type), stringLists, nodeLists);
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
}