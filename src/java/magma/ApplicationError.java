package magma;

public record ApplicationError(Option<String> message, Option<Error> cause) implements Error {
    public static ApplicationError fromCause(Error cause) {
        return new ApplicationError(new None<>(), new Some<>(cause));
    }

    public static ApplicationError fromMessage(String message, String context) {
        return new ApplicationError(new Some<>(message + ": " + context), new None<>());
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
