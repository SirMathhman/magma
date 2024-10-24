package magma.api.option;

import magma.api.Tuple;

import java.util.function.Function;

public record Some<T>(T value) implements Option<T> {
    @Override
    public <R> Option<R> map(Function<T, R> mapper) {
        return new Some<>(mapper.apply(value));
    }

    @Override
    public Tuple<Boolean, T> toTuple(T other) {
        return new Tuple<>(false, value);
    }
}
