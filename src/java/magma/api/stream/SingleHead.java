package magma.api.stream;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;

public final class SingleHead<T> implements Head<T> {
    private final T value;
    private boolean retrieved;

    public SingleHead(T value) {
        this.value = value;
    }

    @Override
    public Option<T> next() {
        if (this.retrieved) return new None<>();
        this.retrieved = true;
        return new Some<>(this.value);
    }
}
