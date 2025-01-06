package magma;

import java.util.ArrayList;
import java.util.List;

public class State {
    public final List<String> segments;
    private final StringBuilder buffer;

    public State(StringBuilder buffer, List<String> segments) {
        this.buffer = buffer;
        this.segments = segments;
    }

    public State() {
        this(new StringBuilder(), new ArrayList<>());
    }

    State append(char c) {
        return new State(this.buffer.append(c), this.segments);
    }

    State advance() {
        if (this.buffer.isEmpty()) return this;

        final var segments = new ArrayList<>(this.segments);
        segments.add(this.buffer.toString());
        return new State(new StringBuilder(), segments);
    }
}
