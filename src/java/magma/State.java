package magma;

import java.util.ArrayList;
import java.util.List;

public class State {
    public final List<String> segments;
    private StringBuilder buffer;
    private int depth;

    public State(List<String> segments, StringBuilder buffer, int depth) {
        this.segments = segments;
        this.buffer = buffer;
        this.depth = depth;
    }

    public State() {
        this(new ArrayList<>(), new StringBuilder(), 0);
    }

    boolean isLevel() {
        return this.depth == 0;
    }

    boolean isShallow() {
        return this.depth == 1;
    }

    State exit() {
        this.depth = this.depth - 1;
        return this;
    }

    State append(char c) {
        this.buffer.append(c);
        return this;
    }

    State enter() {
        this.depth = this.depth + 1;
        return this;
    }

    State advance() {
        if (!this.buffer.isEmpty()) {
            this.segments.add(this.buffer.toString());
            this.buffer = new StringBuilder();
        }
        return this;
    }
}
