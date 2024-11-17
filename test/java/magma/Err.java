package magma;

import java.util.function.Function;

public record Err<T, E>(E error) implements Result<T, E> {
    @Override
    public <R> R match(Function<T, R> onOk, Function<E, R> onErr) {
        return onErr.apply(error);
    }

    @Override
    public boolean isErr() {
        return true;
    }
}
