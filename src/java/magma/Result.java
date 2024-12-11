package magma;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Result<T, X> {
    <R> Result<R, X> mapValue(Function<T, R> mapper);

    <R> R match(Function<T, R> onOk, Function<X, R> onErr);

    Optional<T> findValue();

    Optional<X> findError();

    void consume(Consumer<T> valueConsumer, Consumer<X> errorConsumer);

    <R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper);

    default <R> R into(Function<Result<T, X>, R> mapper) {
        return mapper.apply(this);
    }
}
