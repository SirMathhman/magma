package magma.core.stream;

import magma.core.option.None;
import magma.core.option.Option;
import magma.core.option.Some;

public class SingleHead<T> implements Head<T> {
    private final T value;
    private boolean retrieved = false;

    public SingleHead(T value) {
        this.value = value;
    }

    @Override
    public Option<T> next() {
        if (retrieved) return new None<>();
        retrieved = true;
        return new Some<>(value);
    }
}
