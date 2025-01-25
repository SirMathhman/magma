package magma.app.compile.node;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.api.stream.Stream;
import magma.api.stream.Streams;
import magma.java.JavaMap;
import magma.java.JavaMapCollector;
import magma.java.JavaSet;

import java.util.function.Function;

public record MapNodeProperties<T>(
        JavaMap<String, T> map,
        Function<NodeProperties<T>, Node> completer
) implements NodeProperties<T> {
    public MapNodeProperties(Function<NodeProperties<T>, Node> completer) {
        this(new JavaMap<>(), completer);
    }

    @Override
    public Option<Node> with(String propertyKey, T propertyValue) {
        return complete(this.map.with(propertyKey, propertyValue));
    }

    private Option<Node> complete(Option<JavaMap<String, T>> map) {
        return map
                .<NodeProperties<T>>map(values -> new MapNodeProperties<>(values, this.completer))
                .map(this.completer);
    }

    @Override
    public Option<T> find(String propertyKey) {
        return this.map.find(propertyKey);
    }

    @Override
    public Stream<Tuple<String, T>> stream() {
        return this.map.stream();
    }

    @Override
    public Option<Node> map(String propertyKey, Function<T, T> mapper) {
        return find(propertyKey).map(mapper).flatMap(value -> with(propertyKey, value));
    }

    @Override
    public boolean has(String propertyKey) {
        return this.map.has(propertyKey);
    }

    @Override
    public Option<Node> remove(String propertyKey) {
        return complete(this.map.remove(propertyKey));
    }

    @Override
    public NodeProperties<T> merge(NodeProperties<T> other, MergeStrategy strategy) {
        final var thisKeys = this.map.streamKeys().collect(JavaSet.collect());
        final var otherKeys = other.streamKeys().collect(JavaSet.collect());

        final var newMap = thisKeys.addAll(otherKeys)
                .stream()
                .map(key -> mergeEntry(key, strategy, other))
                .flatMap(Streams::fromOption)
                .collect(new JavaMapCollector<>());

        return new MapNodeProperties<>(newMap, this.completer);
    }

    @Override
    public Stream<String> streamKeys() {
        return this.map.streamKeys();
    }

    private Option<Tuple<String, T>> mergeEntry(
            String key, MergeStrategy strategy, NodeProperties<T> other
    ) {
        final var maybeThisValue = this.map.find(key);
        final var maybeOtherValue = other.find(key);

        return maybeThisValue.and(() -> maybeOtherValue)
                .map(tuple -> strategy.merge(tuple.left(), tuple.right()))
                .or(() -> maybeThisValue)
                .or(() -> maybeOtherValue)
                .map(value -> new Tuple<>(key, value));
    }
}
