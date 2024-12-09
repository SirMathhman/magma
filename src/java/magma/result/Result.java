package magma.result;

import java.util.Optional;
import java.util.function.Consumer;

public interface Result<T, X> {
    void consume(Consumer<T> valueConsumer, Consumer<X> errorConsumer);

    Optional<T> findValue();

    Optional<X> findError();
}
