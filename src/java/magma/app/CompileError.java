package magma.app;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;

public class CompileError implements Error {
    private final String message;
    private final Context context;
    private final Option<Error> cause;

    public CompileError(String message, Context context) {
        this(message, context, new None<>());
    }

    public CompileError(String message, Context context, Option<Error> cause) {
        this.message = message;
        this.context = context;
        this.cause = cause;
    }

    public CompileError(String message, Context context, Error cause) {
        this(message, context, new Some<>(cause));
    }

    @Override
    public String display() {
        final var causeString = cause.map(Error::display).orElse("");
        return message + ": " + context.display() + causeString;
    }
}

