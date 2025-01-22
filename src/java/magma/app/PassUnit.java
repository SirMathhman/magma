package magma.app;

public record PassUnit<T>(State state, T value) {
    public State left() {
        return this.state;
    }

    public T right() {
        return this.value;
    }

    public <R> PassUnit<R> withValue(R value) {
        return new PassUnit<>(this.state, value);
    }
}
