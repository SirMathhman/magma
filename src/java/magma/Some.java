package magma;

public record Some<T>(T value) implements Option<T> {
    @Override
    public boolean isEmpty() {
        return false;
    }
}
