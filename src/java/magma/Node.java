package magma;

import java.util.List;
import java.util.Optional;

public final class Node {
    private final Optional<String> type;
    private final JavaMap<String, String> strings;
    private final JavaMap<String, List<Node>> nodeLists;

    private Node(Optional<String> type, JavaMap<String, String> strings, JavaMap<String, List<Node>> nodeLists) {
        this.type = type;
        this.strings = strings;
        this.nodeLists = nodeLists;
    }

    public Node(String type) {
        this(Optional.of(type));
    }

    public Node(Optional<String> type) {
        this(type, new JavaMap<>(), new JavaMap<>());
    }

    public Node() {
        this(Optional.empty());
    }

    public Node withString(String propertyKey, String propertyValue) {
        return new Node(type, strings.put(propertyKey, propertyValue), nodeLists);
    }

    public Node retype(String type) {
        return new Node(Optional.of(type), strings, nodeLists);
    }

    public boolean is(String type) {
        return this.type.isPresent() && this.type.get().equals(type);
    }

    public Optional<String> findValue(String propertyKey) {
        return strings.find(propertyKey);
    }

    public Node withNodeList(String propertyKey, List<Node> propertyValues) {
        return new Node(type, strings, nodeLists.put(propertyKey, propertyValues));
    }

    public Optional<List<Node>> findNodeList(String propertyKey) {
        return nodeLists.find(propertyKey);
    }
}
