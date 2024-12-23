package magma;

import java.util.ArrayList;
import java.util.List;

public class State {
    private final List<String> segments;
    private final StringBuilder buffer;

    public State(List<String> segments, StringBuilder buffer) {
        this.segments = segments;
        this.buffer = buffer;
    }

    public State() {
        this(new ArrayList<>(), new StringBuilder());
    }

    State append(char c) {
        return new State(segments, buffer.append(c));
    }

    State advance() {
        if (this.buffer.isEmpty()) return this;

        final var copy = new ArrayList<>(this.segments);
        copy.add(this.buffer.toString());
        return new State(copy, new StringBuilder());
    }

    public List<String> segments() {
        return segments;
    }
}
