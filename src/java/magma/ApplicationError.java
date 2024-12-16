package magma;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

public class ApplicationError implements Error {
    private final Option<String> maybeMessage;
    private final Option<Error> maybeCause;

    public ApplicationError(Option<String> maybeMessage, Option<Error> maybeCause) {
        this.maybeMessage = maybeMessage;
        this.maybeCause = maybeCause;
    }

    public ApplicationError(Error maybeCause) {
        this(new None<>(), new Some<>(maybeCause));
    }

    @Override
    public String display() {
        return maybeMessage.match(this::withMessage, this::withoutMessage);
    }

    private String withoutMessage() {
        return maybeCause.match(Error::display, () -> "");
    }

    private String withMessage(String message) {
        return maybeCause.match(cause -> message + ": " + cause.toString(), () -> message);
    }
}
