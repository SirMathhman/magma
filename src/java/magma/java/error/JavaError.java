package magma.java.error;

import magma.compile.Error_;
import magma.core.String_;
import magma.java.JavaString;

public class JavaError implements Error_ {
    private final Exception exception;

    public JavaError(Exception exception) {
        this.exception = exception;
    }

    @Override
    public String_ findMessage() {
        return new JavaString(exception.getMessage());
    }
}
