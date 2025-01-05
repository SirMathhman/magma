package magma;

import java.util.ArrayList;
import java.util.List;

public class State {
    public final List<String> segments;
    public StringBuilder buffer;
    public int depth;

    public State(List<String> segments, StringBuilder buffer, int depth) {
        this.segments = segments;
        this.buffer = buffer;
        this.depth = depth;
    }

    public State() {
        this(new ArrayList<>(), new StringBuilder(), 0);
    }

    State advance() {
        if (!buffer.isEmpty()) {
            segments.add(buffer.toString());
            buffer = new StringBuilder();
        }
        return this;
    }

    State append(char c) {
        buffer.append(c);
        return this;
    }

    public boolean isLevel() {
        return depth == 0;
    }

    public State enter() {
        depth++;
        return this;
    }

    public State exit() {
        depth--;
        return this;
    }

    public boolean isShallow() {
        return depth == 1;
    }
}
