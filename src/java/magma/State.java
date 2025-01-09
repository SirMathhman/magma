package magma;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

public class State {
    private final Deque<Character> queue;
    private final List<String> segments;
    private StringBuilder buffer;
    private int depth;

    public State(Deque<Character> queue, List<String> segments, StringBuilder buffer, int depth) {
        this.queue = queue;
        this.segments = segments;
        this.buffer = buffer;
        this.depth = depth;
    }

    public State(Deque<Character> queue) {
        this(queue, new ArrayList<>(), new StringBuilder(), 0);
    }

    State exit() {
        setDepth(depth() - 1);
        return this;
    }

    State enter() {
        setDepth(depth() + 1);
        return this;
    }

    Optional<Character> append() {
        if (this.queue.isEmpty()) {
            return Optional.empty();
        }
        final var popped = queue().pop();
        append(popped);
        return Optional.of(popped);
    }

    void append(Character c) {
        buffer().append(c);
    }

    State advance() {
        if (!buffer().isEmpty()) segments().add(buffer().toString());
        this.buffer = new StringBuilder();
        return this;
    }

    public Deque<Character> queue() {
        return this.queue;
    }

    public List<String> segments() {
        return this.segments;
    }

    public StringBuilder buffer() {
        return this.buffer;
    }

    public int depth() {
        return this.depth;
    }

    public void setBuffer(StringBuilder buffer) {
        this.buffer = buffer;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
