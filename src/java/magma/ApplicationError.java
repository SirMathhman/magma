package magma;

public class ApplicationError implements Error {
    private final Option<String> message;
    private final Option<Error> cause;

    public ApplicationError(Option<String> message, Option<Error> cause) {
        this.message = message;
        this.cause = cause;
    }

    public ApplicationError(Error cause) {
        this(new None<>(), new Some<>(cause));
    }

    public ApplicationError(String message) {
        this(new Some<>(message), new None<>());
    }

    public static ApplicationError createContextError(String message, String context) {
        return new ApplicationError(message + ": " + context);
    }

    @Override
    public String display() {
        return message.and(() -> cause)
                .map(tuple -> tuple.left() + ": " + tuple.right().display())
                .or(() -> message)
                .or(() -> cause.map(Error::display))
                .orElse("");
    }
}
