package magma.result;

import magma.option.Option;
import magma.option.Some;

import java.util.Optional;

public record Err<T, X>(X error) implements Result<T, X> {
    private Optional<T> findValue0() {
        return Optional.empty();
    }

    private Optional<X> findError0() {
        return Optional.of(this.error);
    }

    @Override
    public Option<T> findValue() {
        return findValue0().<Option<T>>map(Some::new).orElseGet(None::new);
    }

    @Override
    public Option<X> findError() {
        return findError0().<Option<X>>map(Some::new).orElseGet(None::new);
    }
}
