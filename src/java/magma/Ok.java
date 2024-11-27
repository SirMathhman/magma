package magma;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
}
