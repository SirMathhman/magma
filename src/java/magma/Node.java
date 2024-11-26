package magma;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class Node {
    private final Map<String, String> strings;
    private final Map<String, List<String>> stringLists;

    public Node() {
        this(new HashMap<>(), new HashMap<>());
    }

    public Node(Map<String, String> strings, Map<String, List<String>> stringLists) {
        this.strings = strings;
        this.stringLists = stringLists;
    }

    public Node withString(String propertyKey, String propertyValue) {
        strings.put(propertyKey, propertyValue);
        return this;
    }

    public Option<String> findString(String propertyKey) {
        return find(strings, propertyKey);
    }

    private <V> Option<V> find(Map<String, V> map, String key) {
        return map.containsKey(key) ? new Some<>(map.get(key)) : new None<>();
    }

    public Node withStringList(String propertyKey, List<String> propertyValues) {
        stringLists.put(propertyKey, propertyValues);
        return this;
    }

    public Option<List<String>> findStringList(String propertyKey) {
        return find(stringLists, propertyKey);
    }
}
