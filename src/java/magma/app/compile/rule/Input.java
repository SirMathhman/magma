package magma.app.compile.rule;

public record Input(String input) {
    public String getInput() {
        return input;
    }

    public String display() {
        return input;
    }
}