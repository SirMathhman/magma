package magma.result;

import magma.Tuple;
import magma.option.None;
import magma.option.Option;
import magma.option.Some;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record Err<T, X>(X error) implements Result<T, X> {
    @Override
    public boolean isErr() {
        return true;
    }

    @Override
    public Option<X> findErr() {
        return new Some<>(error);
    }

    @Override
    public Option<T> findValue() {
        return new None<>();
    }

    @Override
    public <R> Result<R, X> preserveErr(R replacement) {
        return new Err<>(error);
    }

    @Override
    public void consume(Consumer<T> onValid, Consumer<X> onError) {
        onError.accept(error);
    }

    @Override
    public <R> Result<R, X> mapValue(Function<T, R> mapper) {
        return new Err<>(error);
    }

    @Override
    public <R> Result<Tuple<T, R>, X> and(Supplier<Result<R, X>> supplier) {
        return new Err<>(error);
    }
}
