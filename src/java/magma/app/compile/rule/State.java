package magma.app.compile.rule;

import java.util.ArrayList;
import java.util.List;

public class State {
    private final List<String> segments;
    private final StringBuilder buffer;
    private final int depth;

    public State(List<String> segments, StringBuilder buffer, int depth) {
        this.segments = segments;
        this.buffer = buffer;
        this.depth = depth;
    }

    public State() {
        this(new ArrayList<>(), new StringBuilder(), 0);
    }

    State advance() {
        if (buffer.isEmpty()) return this;

        final var copy = new ArrayList<>(segments);
        copy.add(buffer.toString());
        return new State(copy, new StringBuilder(), depth);
    }

    State append(char c) {
        return new State(segments, buffer.append(c), depth);
    }

    public List<String> segments() {
        return segments;
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

    public boolean isShallow() {
        return depth == 1;
    }
}
