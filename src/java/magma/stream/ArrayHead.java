package magma.stream;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

public class ArrayHead<T> implements Head<T> {
    private final T value;
    private boolean retrieved;

    public ArrayHead(T value) {
        this.value = value;
        this.retrieved = false;
    }

    @Override
    public Option<T> next() {
        if (this.retrieved) return new None<>();
        this.retrieved = true;
        return new Some<>(this.value);
    }
}
