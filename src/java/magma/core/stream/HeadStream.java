package magma.core.stream;

import magma.core.option.Option;

import java.util.function.Function;
import java.util.function.Predicate;

public record HeadStream<T>(Head<T> head) implements Stream<T> {
    @Override
    public <R> Stream<R> map(Function<T, R> mapper) {
        return new HeadStream<>(() -> head.next().map(mapper));
    }

    @Override
    public Stream<T> filter(Predicate<T> predicate) {
        return flatMap(value -> predicate.test(value)
                ? new SingleHead<>(value)
                : new EmptyHead<>());
    }

    private <R> Stream<R> flatMap(Function<T, Head<R>> mapper) {
        return head.next()
                .map(mapper)
                .map(initial -> new HeadStream<>(new FlatMapHead<>(head, initial, mapper)))
                .orElseGet(() -> new HeadStream<>(new EmptyHead<>()));
    }

    @Override
    public Option<T> next() {
        return head.next();
    }
}
