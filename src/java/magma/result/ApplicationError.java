package magma.result;

import magma.error.Error;

public record ApplicationError(magma.error.Error cause) implements Error {
    @Override
    public String display() {
        return this.cause.display();
    }
}
