package magma.api.stream;

import magma.api.Tuple;
import magma.api.option.Option;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public record HeadedStream<T>(Head<T> head) implements Stream<T> {
    @Override
    public <R> Stream<R> map(Function<T, R> mapper) {
        return new HeadedStream<>(() -> head.next().map(mapper));
    }

    @Override
    public <R> R into(Function<Stream<T>, R> mapper) {
        return mapper.apply(this);
    }

    @Override
    public <C> C foldLeft(C initial, BiFunction<C, T, C> folder) {
        var current = initial;
        while (true) {
            C finalCurrent = current;
            final var tuple = head.next()
                    .map(next -> folder.apply(finalCurrent, next))
                    .toTuple(current);

            if (tuple.left()) {
                current = tuple.right();
            } else {
                return current;
            }
        }
    }

    @Override
    public Option<T> foldLeft(BiFunction<T, T, T> folder) {
        return head.next().map(inner -> foldLeft(inner, folder));
    }

    @Override
    public Stream<T> filter(Predicate<T> predicate) {
        return flatMap(value -> predicate.test(value)
                ? new HeadedStream<>(new SingleHead<>(value))
                : new HeadedStream<>(new EmptyHead<>()));
    }

    @Override
    public <R> Stream<R> flatMap(Function<T, Stream<R>> mapper) {
        return map(mapper).foldLeft(new HeadedStream<>(new EmptyHead<>()), new BiFunction<HeadedStream<R>, Stream<R>, HeadedStream<R>>() {
            @Override
            public HeadedStream<R> apply(HeadedStream<R> rHeadedStream, Stream<R> rStream) {
                return rHeadedStream.concat(rStream);
            }
        });
    }

    private HeadedStream<T> concat(Stream<T> other) {
        return new HeadedStream<>(() -> head.next().or(other::next));
    }

    @Override
    public Option<T> next() {
        return head.next();
    }

    @Override
    public <R> Stream<Tuple<T, R>> extend(Function<T, R> mapper) {
        return map(value -> new Tuple<>(value, mapper.apply(value)));
    }
}
