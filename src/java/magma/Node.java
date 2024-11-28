package magma;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Node(
        Option<String> type,
        Map<String, Integer> integers,
        Map<String, String> strings,
        Map<String, List<Node>> nodeLists) {
    public Node(String type) {
        this(new Some<>(type), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    public Node withInt(String propertyKey, int propertyValue) {
        integers.put(propertyKey, propertyValue);
        return this;
    }

    public Node withString(String propertyKey, String propertyValue) {
        strings.put(propertyKey, propertyValue);
        return this;
    }

    public Node withNodeList(String propertyKey, List<Node> propertyValues) {
        nodeLists.put(propertyKey, propertyValues);
        return this;
    }

    public boolean is(String type) {
        return this.type.filter(value -> value.equals(type)).isPresent();
    }

    public Option<String> findString(String propertyKey) {
        return strings.containsKey(propertyKey)
                ? new Some<>(strings.get(propertyKey))
                : new None<>();
    }

    public Option<List<Node>> findNodeList(String propertyKey) {
        return nodeLists.containsKey(propertyKey)
                ? new Some<>(nodeLists.get(propertyKey))
                : new None<>();
    }

    public Option<Integer> findInt(String propertyKey) {
        return integers.containsKey(propertyKey)
                ? new Some<>(integers.get(propertyKey))
                : new None<>();
    }
}
