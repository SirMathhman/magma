package magma;

import magma.app.Context;

public record StringContext(String input) implements Context {
    @Override
    public String display() {
        return input;
    }
}
