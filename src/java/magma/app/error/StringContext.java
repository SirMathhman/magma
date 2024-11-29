package magma.app.error;

public record StringContext(String value) implements Context {
    @Override
    public String display() {
        return "'" + value + "'";
    }
}