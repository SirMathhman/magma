package magma.app.compile;

public record State(int depth) {
    public State() {
        this(0);
    }

    public State enter() {
        return new State(depth + 1);
    }

    public State exit() {
        return new State(depth - 1);
    }
}
