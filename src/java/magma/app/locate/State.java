package magma.app.locate;

public record State(int depth) {
    public boolean isShallow() {
        return depth == 1;
    }
}
