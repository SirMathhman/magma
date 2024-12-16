package magma.option;

public record Some<T>(T error) implements Option<T> {
    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public T orElseNull() {
        return error;
    }
}
