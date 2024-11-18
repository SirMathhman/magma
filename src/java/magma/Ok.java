package magma;

import java.util.function.Function;

public record Ok<T, E>(T value) implements Result<T, E> {
    @Override
    public <R> R match(Function<T, R> onOk, Function<E, R> onErr) {
        return onOk.apply(value);
    }

    @Override
    public boolean isErr() {
        return false;
    }
}
