package magma.app.compile.error;

public record StringContext(String input) implements Context {
    @Override
    public String asString() {
        return input;
    }
}