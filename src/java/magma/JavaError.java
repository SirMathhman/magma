package magma;

import magma.app.Error;

public record JavaError(Exception e) implements Error {
    @Override
    public String display() {
        return e.getMessage();
    }
}
