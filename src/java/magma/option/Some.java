package magma.option;

public record Some<T>(T value) implements Option<T> {
    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public T orElseNull() {
        return value;
    }
}
