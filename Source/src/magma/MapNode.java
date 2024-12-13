package magma;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class MapNode implements Node {
    private final Map<String, List<String>> stringLists;

    public MapNode(Map<String, List<String>> stringLists) {
        this.stringLists = stringLists;
    }

    public MapNode() {
        this(new HashMap<>());
    }

    @Override
    public Optional<List<String>> findStringList(String propertyKey) {
        return Optional.ofNullable(stringLists.get(propertyKey));
    }

    @Override
    public Node withStringList(String propertyKey, List<String> propertyValues) {
        stringLists.put(propertyKey, propertyValues);
        return this;
    }
}