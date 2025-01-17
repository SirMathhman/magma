package magma.api.result;

import magma.api.error.Error;

public record ApplicationError(magma.api.error.Error cause) implements Error {
    @Override
    public String display() {
        return this.cause.display();
    }
}
