package magma.app.compile;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record MapNode(
        Option<String> type, Map<String, String> strings,
        Map<String, List<Node>> nodeLists
) implements Node {
    public MapNode() {
        this(new None<>(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Override
    public Option<String> findString(String propertyKey) {
        return strings.containsKey(propertyKey)
                ? new Some<>(strings.get(propertyKey))
                : new None<>();
    }

    @Override
    public String asString() {
        /*
        TODO: this is a stub for now
         */
        return toString();
    }

    @Override
    public Option<List<Node>> findNodeList(String propertyKey) {
        return nodeLists.containsKey(propertyKey)
                ? new Some<>(nodeLists.get(propertyKey))
                : new None<>();
    }

    @Override
    public Option<Node> withNodeList(String propertyKey, List<Node> propertyValues) {
        if (nodeLists.containsKey(propertyKey)) return new None<>();

        final var copy = new HashMap<>(nodeLists);
        copy.put(propertyKey, propertyValues);
        return new Some<>(new MapNode(type, strings, copy));
    }

    @Override
    public Option<Node> withString(String propertyKey, String propertyValue) {
        if (strings.containsKey(propertyKey)) return new None<>();

        final var copy = new HashMap<>(strings);
        copy.put(propertyKey, propertyValue);
        return new Some<>(new MapNode(type, copy, nodeLists));
    }

    @Override
    public Option<Node> retype(String type) {
        if (this.type.isPresent()) return new None<>();

        return new Some<>(new MapNode(new Some<>(type), strings, nodeLists));
    }

    @Override
    public boolean is(String type) {
        return this.type
                .map(value -> value.equals(type))
                .orElse(false);
    }
}