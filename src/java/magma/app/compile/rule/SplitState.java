package magma.app.compile.rule;

import java.util.ArrayList;
import java.util.List;

public class SplitState {
    private final List<String> segments;
    private final StringBuilder buffer;
    private final int depth;

    public SplitState(List<String> segments, StringBuilder buffer, int depth) {
        this.segments = segments;
        this.buffer = buffer;
        this.depth = depth;
    }

    public SplitState() {
        this(new ArrayList<>(), new StringBuilder(), 0);
    }

    SplitState advance() {
        if (buffer.isEmpty()) return this;

        final var copy = new ArrayList<>(segments);
        copy.add(buffer.toString());
        return new SplitState(copy, new StringBuilder(), depth);
    }

    SplitState append(char c) {
        return new SplitState(segments, buffer.append(c), depth);
    }

    public List<String> segments() {
        return segments;
    }

    public boolean isLevel() {
        return depth == 0;
    }

    public SplitState enter() {
        return new SplitState(segments, buffer, depth + 1);
    }

    public SplitState exit() {
        return new SplitState(segments, buffer, depth - 1);
    }

    public boolean isShallow() {
        return depth == 1;
    }
}
