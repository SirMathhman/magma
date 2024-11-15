package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.java.JavaList;

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

    public Option<Tuple<State, JavaList<Node>>> loadLabel(String label) {
        return new None<>();
    }
}
