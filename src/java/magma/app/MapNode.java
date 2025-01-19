package magma.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class MapNode implements Node {
    private final Map<String, String> strings;
    private final Map<String, List<Node>> nodeLists;
    private final Map<String, Node> nodes;

    public MapNode() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    public MapNode(Map<String, String> strings, Map<String, List<Node>> nodeLists, Map<String, Node> nodes) {
        this.strings = strings;
        this.nodeLists = nodeLists;
        this.nodes = nodes;
    }

    @Override
    public Optional<Node> findNode(String propertyKey) {
        return Optional.ofNullable(this.nodes.get(propertyKey));
    }

    @Override
    public Node mapString(String propertyKey, Function<String, String> mapper) {
        return findString(propertyKey).map(mapper).map(newString -> withString(propertyKey, newString)).orElse(this);
    }

    @Override
    public Node merge(Node other) {
        var current = other;
        for (Map.Entry<String, String> entry : this.strings.entrySet()) {
            current = current.withString(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Node> entry : this.nodes.entrySet()) {
            current = current.withNode(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, List<Node>> entry : this.nodeLists.entrySet()) {
            current = current.withNodeList(entry.getKey(), entry.getValue());
        }
        return current;
    }

    @Override
    public Node withNode(String propertyKey, Node propertyValue) {
        this.nodes.put(propertyKey, propertyValue);
        return this;
    }

    @Override
    public Node withNodeList(String propertyKey, List<Node> propertyValues) {
        this.nodeLists.put(propertyKey, propertyValues);
        return this;
    }

    @Override
    public Optional<List<Node>> findNodeList(String propertyKey) {
        return Optional.ofNullable(this.nodeLists.get(propertyKey));
    }

    @Override
    public Node withString(String propertyKey, String propertyValues) {
        this.strings.put(propertyKey, propertyValues);
        return this;
    }

    @Override
    public Optional<String> findString(String propertyKey) {
        return Optional.ofNullable(this.strings.get(propertyKey));
    }
}
