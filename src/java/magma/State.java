package magma;

import java.util.ArrayList;
import java.util.List;

public class State {
    public final List<String> segments;
    private final StringBuilder buffer;
    private final int depth;

    public State(List<String> segments, StringBuilder buffer, int depth) {
        this.buffer = buffer;
        this.segments = segments;
        this.depth = depth;
    }

    public State() {
        this(new ArrayList<>(), new StringBuilder(), 0);
    }

    State append(char c) {
        return new State(this.segments, this.buffer.append(c), this.depth);
    }

    State advance() {
        if (this.buffer.isEmpty()) return this;

        final var segments = new ArrayList<>(this.segments);
        segments.add(this.buffer.toString());
        return new State(segments, new StringBuilder(), this.depth);
    }

    public boolean isLevel() {
        return this.depth == 0;
    }

    public State enter() {
        return new State(this.segments, this.buffer, this.depth + 1);
    }

    public State exit() {
        return new State(this.segments, this.buffer, this.depth - 1);
    }
}
