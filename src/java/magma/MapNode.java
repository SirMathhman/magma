package magma;

import magma.rule.Properties;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class MapNode implements Node {
    private final Optional<String> type;
    private final Properties<String> strings;
    private final Properties<List<Node>> nodeLists;

    public MapNode() {
        this.type = Optional.empty();
        this.strings = new MapProperties<>(this::withStrings);
        this.nodeLists = new MapProperties<>(this::withNodeLists);
    }

    public MapNode(Optional<String> type, Properties<String> strings, Properties<List<Node>> nodeLists) {
        this.strings = strings;
        this.type = type;
        this.nodeLists = nodeLists;
    }

    private Node withNodeLists(Properties<List<Node>> nodeLists) {
        return new MapNode(type, strings, nodeLists);
    }

    private Node withStrings(Properties<String> strings) {
        return new MapNode(type, strings, nodeLists);
    }

    @Override
    public Properties<String> strings() {
        return strings;
    }

    @Override
    public Properties<List<Node>> nodeLists() {
        return nodeLists;
    }

    @Override
    public Node retype(String type) {
        return new MapNode(Optional.of(type), strings, nodeLists);
    }

    @Override
    public boolean is(String type) {
        return this.type.filter(inner -> inner.equals(type)).isPresent();
    }

    private static class MapProperties<T> implements Properties<T> {
        private final Map<String, T> strings;
        private final Function<Properties<T>, Node> clone;

        private MapProperties(Function<Properties<T>, Node> clone) {
            this(clone, Collections.emptyMap());
        }

        private MapProperties(Function<Properties<T>, Node> clone, Map<String, T> strings) {
            this.strings = strings;
            this.clone = clone;
        }

        @Override
        public Node with(String propertyKey, T propertyValue) {
            final var copy = new HashMap<>(strings);
            copy.put(propertyKey, propertyValue);
            return clone.apply(new MapProperties<>(clone, copy));
        }

        @Override
        public Optional<T> find(String propertyKey) {
            return Optional.ofNullable(strings.get(propertyKey));
        }

        @Override
        public Optional<Node> map(String propertyKey, Function<T, T> mapper) {
            return find(propertyKey).map(mapper).map(value -> with(propertyKey, value));
        }
    }
}