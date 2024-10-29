package magma.compile;

import magma.core.String_;
import magma.core.option.Option;
import magma.java.JavaMap;
import magma.java.JavaString;

public final class MapNode implements Node {
    private final JavaMap<String_, String_> strings;

    public MapNode() {
        this(new JavaMap<>());
    }

    public MapNode(JavaMap<String_, String_> strings) {
        this.strings = strings;
    }

    @Override
    public Option<String_> find(String propertyKey) {
        return strings.find(new JavaString(propertyKey));
    }

    @Override
    public Option<Node> withString(String propertyKey, String_ propertyValue) {
        return strings.put(new JavaString(propertyKey), propertyValue).map(MapNode::new);
    }
}