package magma.api.option;

import magma.api.Tuple;

import java.util.function.Function;

public interface Option<T> {
    <R> Option<R> map(Function<T, R> mapper);

    Tuple<Boolean, T> toTuple(T other);
}
