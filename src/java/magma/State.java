package magma;

import java.util.List;

public class State {
    private final List<String> segments;
    private final StringBuilder buffer;

    public State(List<String> segments, StringBuilder buffer) {
        this.segments = segments;
        this.buffer = buffer;
    }

    State append(char c) {
        return new State(segments, buffer.append(c));
    }

    State advance() {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
        return new State(segments, new StringBuilder());
    }

    public List<String> getSegments() {
        return segments;
    }
}
