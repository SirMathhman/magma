package magma;

import java.util.ArrayList;
import java.util.List;

public class State {
    public final List<String> segments;
    public final StringBuilder buffer;
    public final int depth;

    public State(List<String> segments, StringBuilder buffer, int depth) {
        this.buffer = buffer;
        this.segments = segments;
        this.depth = depth;
    }

    public State() {
        this(new ArrayList<>(), new StringBuilder(), 0);
    }

    State advance() {
        if (this.buffer.isEmpty()) return this;

        final var copy = new ArrayList<>(this.segments);
        copy.add(this.buffer.toString());
        return new State(copy, new StringBuilder(), depth);
    }

    State append(char c) {
        return new State(segments, buffer.append(c), depth);
    }

    public boolean isLevel() {
        return depth == 0;
    }

    public State enter() {
        return new State(segments, buffer, depth + 1);
    }

    public State exit() {
        return new State(segments, buffer, depth - 1);
    }
}
