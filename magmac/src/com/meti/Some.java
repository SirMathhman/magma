package com.meti;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public record Some<T>(T value) implements Option<T> {
    public static <T> Option<T> apply(T value) {
        return new Some<>(value);
    }

    @Override
    public <R> Option<R> replaceValue(R value) {
        return new Some<>(value);
    }

    @Override
    public Option<T> filter(Predicate<T> predicate) {
        return predicate.test(value) ? this : None.apply();
    }

    @Override
    public <R> Option<R> map(Function<T, R> mapper) {
        return Some.apply(mapper.apply(value));
    }

    @Override
    public T unwrapOrElseGet(Supplier<T> other) {
        return value;
    }

    @Override
    public Option<T> orElseGet(Supplier<Option<T>> other) {
        return this;
    }

    @Override
    public T $() {
        return value;
    }
}
