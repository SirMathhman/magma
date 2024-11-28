package magma.result;

import magma.Tuple;
import magma.option.None;
import magma.option.Option;
import magma.option.Some;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record Ok<T, X>(T value) implements Result<T, X> {
    @Override
    public boolean isErr() {
        return false;
    }

    @Override
    public Option<X> findErr() {
        return new None<>();
    }

    @Override
    public Option<T> findValue() {
        return new Some<>(value);
    }

    @Override
    public <R> Result<R, X> preserveErr(R replacement) {
        return new Ok<>(replacement);
    }

    @Override
    public void consume(Consumer<T> onValid, Consumer<X> onError) {
        onValid.accept(value);
    }

    @Override
    public <R> Result<R, X> mapValue(Function<T, R> mapper) {
        return new Ok<>(mapper.apply(value));
    }

    @Override
    public <R> Result<Tuple<T, R>, X> and(Supplier<Result<R, X>> supplier) {
        return supplier.get().mapValue(otherValue -> new Tuple<>(value, otherValue));
    }
}
