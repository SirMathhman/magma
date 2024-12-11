package magma;

public record RuntimeError(String message, String context) {
    public String display() {
        return message + ": " + context;
    }
}
