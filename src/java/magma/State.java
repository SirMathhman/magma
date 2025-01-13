package magma;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

public class State {
    public final List<String> segments;
    public final Deque<Character> queue;
    public int depth;
    private StringBuilder buffer;

    public State(List<String> segments, StringBuilder buffer, int depth, Deque<Character> queue) {
        this.segments = segments;
        this.buffer = buffer;
        this.depth = depth;
        this.queue = queue;
    }

    public State(Deque<Character> queue) {
        this(new ArrayList<>(), new StringBuilder(), 0, queue);
    }

    Optional<Tuple<State, Character>> pop() {
        if (this.queue.isEmpty()) return Optional.empty();
        return Optional.of(new Tuple<>(this, this.queue.pop()));
    }

    boolean isLevel() {
        return this.depth == 0;
    }

    boolean isShallow() {
        return this.depth == 1;
    }

    State exit() {
        if (this.depth == 0) {
            Results.writeErr("Depth cannot be negative.", "", "");
        }

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

    Optional<State> appendFromQueue() {
        return appendAndPop().map(Tuple::left);
    }

    Optional<Tuple<State, Character>> appendAndPop() {
        return pop().map(tuple -> tuple.mergeIntoLeft(State::append));
    }

    public Optional<Character> peek() {
        return this.queue.isEmpty() ? Optional.empty() : Optional.of(this.queue.peek());
    }
}
