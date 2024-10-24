package magma.api.stream;

import java.util.function.BiFunction;

public record HeadedStream<T>(Head<T> head) implements Stream<T> {
    @Override
    public <C> C collect(Collector<T, C> collector) {
        return foldRight(collector.initial(), collector::fold);
    }

    @Override
    public <C> C foldRight(C initial, BiFunction<C, T, C> folder) {
        var current = initial;
        while (true) {
            C finalCurrent = current;
            final var tuple = head.next()
                    .map(value -> folder.apply(finalCurrent, value))
                    .toTuple(current);

            if (tuple.left()) {
                current = tuple.right();
            } else {
                return current;
            }
        }
    }
}
