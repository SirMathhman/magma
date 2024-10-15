package magma.app.compile;

import java.util.*;
import java.util.function.Function;

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
    public Optional<List<Node>> findNodeList(String propertyKey) {
        return Optional.ofNullable(nodeLists.get(propertyKey));
    }

    @Override
    public Node withNodeList(String propertyKey, List<Node> values) {
        final var copy = new HashMap<>(nodeLists);
        copy.put(propertyKey, values);
        return new MapNode(type, strings, copy);
    }
}
