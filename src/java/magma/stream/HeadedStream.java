package magma.stream;

import magma.result.Ok;
import magma.result.Result;

import java.util.function.BiFunction;

public record HeadedStream<T>(Head<T> head) implements Stream<T> {
    @Override
    public <R, X> Result<R, X> foldLeftIntoResult(R initial, BiFunction<R, T, Result<R, X>> folder) {
        return this.<Result<R, X>>foldLeft(new Ok<>(initial), (current, next) -> current.flatMapValue(currentValue -> folder.apply(currentValue, next)));
    }

    private <R> R foldLeft(R initial, BiFunction<R, T, R> folder) {
        var current = initial;
        while (true) {
            var finalCurrent = current;
            final var tuple = head.next()
                    .map(headValue -> folder.apply(finalCurrent, headValue))
                    .toTuple(current);

            if (tuple.left()) {
                current = tuple.right();
            } else {
                return current;
            }
        }
    }
}
