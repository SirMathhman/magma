package magma.app.error;

import magma.app.Input;

public final class InputContext implements Context {
    private final Input input;

    public InputContext(Input input) {
        this.input = input;
    }

    @Override
    public String display() {
        return input.display();
    }
}
