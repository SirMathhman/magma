package magma.error;

public record StringContext(String context) implements Context {
    @Override
    public String format() {
        return context();
    }
}