package magma.app.rule.locate;

public record State(int depth) {
    public boolean isShallow() {
        return depth == 1;
    }
}
