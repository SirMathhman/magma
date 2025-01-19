package magma;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
