package magma;

class State {
    private int depth;

    State() {
        this(0);
    }

    State(int depth) {
        this.depth = depth;
    }

    public State exit() {
        if (depth == 0) {
            throw new IllegalStateException("Depth cannot be less than 0!");
        }
        depth--;
        return this;
    }

    public State enter() {
        depth++;
        return this;
    }

    public int depth() {
        return depth;
    }
}
