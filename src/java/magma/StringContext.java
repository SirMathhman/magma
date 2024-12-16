package magma;

import magma.error.Context;

public record StringContext(String value) implements Context {
    @Override
    public String display() {
        return value;
    }
}
