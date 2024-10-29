package magma.compile;

import magma.core.String_;

public class CompileError implements Error_ {
    private final String_ message;

    public CompileError(String_ message) {
        this.message = message;
    }

    public static CompileError create(String message, String_ context) {
        return new CompileError(context.prependSlice(message + ": "));
    }

    public static CompileError create(String message, Node context) {
        return create(message, context.format());
    }

    @Override
    public String_ findMessage() {
        return message;
    }
}
