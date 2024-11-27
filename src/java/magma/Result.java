package magma;

import java.util.function.Consumer;

public interface Result<T, X> {
    boolean isErr();

    Option<X> findErr();

    Option<T> findValue();

    <R> Result<R, X> preserveErr(R replacement);

    void consume(Consumer<T> onValid, Consumer<X> onError);
}
