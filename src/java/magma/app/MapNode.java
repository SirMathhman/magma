package magma.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;

public final class MapNode implements Node {
    private final Map<String, String> strings;
    private final Map<String, List<Node>> nodeLists;
    private final Map<String, Node> nodes;
    private final Optional<String> type;

    public MapNode() {
        this(Optional.empty(), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    public MapNode(Optional<String> type, Map<String, String> strings, Map<String, Node> nodes, Map<String, List<Node>> nodeLists) {
        this.type = type;
        this.strings = strings;
        this.nodes = nodes;
        this.nodeLists = nodeLists;
    }

    @Override
    public String toString() {
        final var typeString = this.type.map(inner -> inner + " ").orElse("");

        var builder = new StringBuilder()
                .append(typeString)
                .append("{");

        final var joiner = new StringJoiner(",");
        for (Map.Entry<String, String> entry : this.strings.entrySet()) {
            joiner.add(new StringBuilder()
                    .append("\n\t")
                    .append(entry.getKey())
                    .append(" : \"")
                    .append(entry.getValue())
                    .append("\""));
        }

        builder.append(joiner);
        return builder.append("\n}").toString();
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
    public String display() {
        return toString();
    }

    @Override
    public Node retype(String type) {
        return new MapNode(Optional.of(type), this.strings, this.nodes, this.nodeLists);
    }

    @Override
    public boolean is(String type) {
        return this.type.isPresent() && this.type.get().equals(type);
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
