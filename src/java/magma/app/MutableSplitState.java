package magma.app;

import java.util.ArrayList;
import java.util.List;

public class MutableSplitState implements SplitState {
    private final List<String> segments;
    private StringBuilder buffer;
    private int depth;

    public MutableSplitState(List<String> segments, StringBuilder buffer, int depth) {
        this.segments = segments;
        this.buffer = buffer;
        this.depth = depth;
    }

    public MutableSplitState() {
        this(new ArrayList<>(), new StringBuilder(), 0);
    }

    @Override
    public SplitState enter() {
        this.depth = depth + 1;
        return this;
    }

    @Override
    public SplitState next(char c) {
        buffer.append(c);
        return this;
    }

    @Override
    public SplitState exit() {
        this.depth = depth - 1;
        return this;
    }

    @Override
    public SplitState advance() {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
        this.buffer = new StringBuilder();
        return this;
    }

    @Override
    public boolean isShallow() {
        return depth == 1;
    }

    @Override
    public boolean isLevel() {
        return depth == 0;
    }

    @Override
    public List<String> asList() {
        return segments;
    }
}
