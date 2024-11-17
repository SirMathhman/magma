package magma;

import java.util.function.Function;

public interface Result<T, E> {
    <R> R match(Function<T, R> onOk, Function<E, R> onErr);

    boolean isErr();
}
