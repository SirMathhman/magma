package magma;

import java.io.IOException;

public record JavaError(Exception exception) implements Error {
    @Override
    public String display() {
        return exception.getMessage();
    }
}
