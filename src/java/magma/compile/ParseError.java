package magma.compile;

import magma.core.String_;

public class ParseError implements Error_ {
    private final String_ message;

    public ParseError(String_ message) {
        this.message = message;
    }

    public static Error_ create(String message, String_ context) {
        return new ParseError(context.prependSlice(message + ": "));
    }

    @Override
    public String_ findMessage() {
        return message;
    }
}
