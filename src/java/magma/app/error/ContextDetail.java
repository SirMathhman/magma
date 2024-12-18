package magma.app.error;

public record ContextDetail(String message, Context context) implements Detail {
    @Override
    public String display() {
        return message() + ": " + context().display();
    }
}