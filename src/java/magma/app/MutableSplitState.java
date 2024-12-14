package magma.app;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class MutableSplitState implements SplitState {
    private final List<String> segments;
    private StringBuilder buffer;
    private int depth;
    private final Deque<Character> queue;

    public MutableSplitState(Deque<Character> queue, List<String> segments, StringBuilder buffer, int depth) {
        this.segments = segments;
        this.buffer = buffer;
        this.depth = depth;
        this.queue = queue;
    }

    public MutableSplitState(Deque<Character> queue) {
        this(queue, new ArrayList<>(), new StringBuilder(), 0);
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

    @Override
    public Option<Tuple<Character, SplitState>> pop() {
        if(queue.isEmpty()) return new None<>();

        final var popped = queue.pop();
        return new Some<>(new Tuple<>(popped, this));
    }
}
