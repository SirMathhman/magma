package magma;

import java.util.List;

public class State {
    private final List<String> segments;
    private final StringBuilder buffer;
    private final int depth;

    public State(List<String> segments, StringBuilder buffer) {
        this(segments, buffer, 0);
    }

    public State(List<String> segments, StringBuilder buffer, int depth) {
        this.segments = segments;
        this.buffer = buffer;
        this.depth = depth;
    }

    State append(char c) {
        return new State(segments, buffer.append(c), depth);
    }

    State advance() {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
        return new State(segments, new StringBuilder(), depth);
    }

    public List<String> getSegments() {
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
}
