package magma.app;

public record State(int depth) {
    public State() {
        this(0);
    }


    public State exit() {
        return new State(this.depth - 1);
    }

    public State enter() {
        return new State(this.depth + 1);
    }
}
