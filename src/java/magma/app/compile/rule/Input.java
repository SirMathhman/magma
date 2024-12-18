package magma.app.compile.rule;

public record Input(String input, int start, int length) {
    public Input(String input) {
        this(input, 0, input.length());
    }

    public String getInput() {
        return input;
    }

    public String display() {
        var buffer = new StringBuilder().append("\n");
        final var slices = computeSlice().split("\\n");
        for (int i = 0; i < slices.length; i++) {
            var slice = slices[i];
            buffer.append(slice).append("\n");
            buffer.append("^".repeat(slice.length())).append("\n");
        }
        return buffer.toString();
    }

    private String computeSlice() {
        return input.substring(start, start + length);
    }
}