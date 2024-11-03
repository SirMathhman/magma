package magma.app.compile.error;

import magma.app.Error;

public record CompileError(String message, Context context) implements Error {
    @Override
    public String asString() {
        return message + ": " + context.asString();
    }
}
