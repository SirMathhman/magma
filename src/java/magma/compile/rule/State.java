package magma.compile.rule;

import java.util.ArrayList;
import java.util.List;

public record State(List<String> segments, StringBuilder buffer, int depth) {
    public State() {
        this(new ArrayList<>(), new StringBuilder(), 0);
    }

    State append(char c) {
        return new State(segments, buffer.append(c), depth);
    }

    public State advance() {
        if (buffer.isEmpty()) return this;
        final var copy = new ArrayList<>(segments);
        copy.add(buffer.toString());
        return new State(copy, new StringBuilder(), depth);
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
