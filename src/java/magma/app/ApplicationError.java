package magma.app;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;

public class ApplicationError implements Error {
    private final Option<String> maybeMessage;
    private final Option<Error> maybeCause;

    public ApplicationError(Option<String> maybeMessage, Option<Error> maybeCause) {
        this.maybeMessage = maybeMessage;
        this.maybeCause = maybeCause;
    }

    public ApplicationError(String maybeMessage) {
        this(new Some<>(maybeMessage), new None<>());
    }

    public ApplicationError(String maybeMessage, Error maybeCause) {
        this(new Some<>(maybeMessage), new Some<>(maybeCause));
    }

    public ApplicationError(Error maybeCause) {
        this(new None<>(), new Some<>(maybeCause));
    }

    @Override
    public String display() {
        return maybeMessage.match(this::withMessage, this::withoutMessage);
    }

    private String withoutMessage() {
        return maybeCause.map(Error::display).orElse("");
    }

    private String withMessage(String message) {
        return maybeCause.match(cause -> message + ": " + cause.display(), () -> message);
    }
}
