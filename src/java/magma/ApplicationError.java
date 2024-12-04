package magma;

import magma.api.error.Error;

public record ApplicationError(magma.api.error.Error e) implements Error {
    @Override
    public String display() {
        return e.display();
    }
}
