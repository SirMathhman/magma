package magma;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class MapNode implements Node {
    private final Map<String, String> strings;
    private final Map<String, List<Node>> nodeLists;

    public MapNode() {
        this(new HashMap<>(), new HashMap<>());
    }

    public MapNode(Map<String, String> strings, Map<String, List<Node>> nodeLists) {
        this.strings = strings;
        this.nodeLists = nodeLists;
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
