package magma.api.option;

import magma.api.Tuple;

import java.util.function.Function;

public class None<T> implements Option<T> {
    @Override
    public <R> Option<R> map(Function<T, R> mapper) {
        return new None<>();
    }

    @Override
    public Tuple<Boolean, T> toTuple(T other) {
        return new Tuple<>(false, other);
    }
}
