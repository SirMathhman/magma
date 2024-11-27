package magma;

public record RuntimeError(String message) {
    public String display() {
        return message;
    }
}
