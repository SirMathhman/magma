package magma;

import java.util.ArrayList;
import java.util.List;

public class State {
    public final List<String> segments;
    public StringBuilder buffer;

    public State(List<String> segments, StringBuilder buffer) {
        this.buffer = buffer;
        this.segments = segments;
    }

    public State() {
        this(new ArrayList<>(), new StringBuilder());
    }

    State advance() {
        if (this.buffer.isEmpty()) return this;

        final var copy = new ArrayList<>(this.segments);
        copy.add(this.buffer.toString());
        return new State(copy, new StringBuilder());
    }

    State append(char c) {
        return new State(segments, buffer.append(c));
    }
}
