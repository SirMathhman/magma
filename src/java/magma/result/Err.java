package magma.result;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

import java.util.function.Consumer;

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
}
