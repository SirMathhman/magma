package magma.option;

import magma.Tuple;

import java.util.function.Function;

public interface Option<T> {
    boolean isPresent();

    T orElseNull();

    <R> Option<R> map(Function<T, R> mapper);

    Tuple<Boolean, T> toTuple(T other);

    String display();
}
