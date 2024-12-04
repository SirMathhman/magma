package magma.java;

import magma.api.error.Error;

public record JavaError(Exception exception) implements Error {
    @Override
    public String display() {
        return exception.getMessage();
    }
}
