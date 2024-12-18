package magma.app.compile.rule;

public record Input(String input, int start, int length) {
    public Input(String input) {
        this(input, 0, input.length());
    }

    public String getInput() {
        return input;
    }

    @Override
    public String toString() {
        return computeSlice();
    }

    public String display() {
        return computeSlice();
    }

    private String computeSlice() {
        return input.substring(start, start + length);
    }
}