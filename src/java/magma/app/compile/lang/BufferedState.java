package magma.app.compile.lang;

import magma.api.Tuple;
import magma.api.stream.Stream;
import magma.java.JavaList;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

record BufferedState(Deque<Character> queue, List<String> segments, StringBuilder buffer, int depth) {
    public BufferedState(Deque<Character> queue) {
        this(queue, new ArrayList<>(), new StringBuilder(), 0);
    }

    boolean isLevel() {
        return depth() == 0;
    }

    BufferedState advance() {
        if (buffer.isEmpty()) return this;

        final var copy = new ArrayList<>(segments);
        copy.add(buffer.toString());
        return new BufferedState(queue, copy, new StringBuilder(), depth);
    }

    Optional<Tuple<BufferedState, Character>> pop() {
        if (queue.isEmpty()) return Optional.empty();
        return Optional.of(new Tuple<>(this, queue.pop()));
    }

    BufferedState append(Character c) {
        buffer().append(c);
        return this;
    }

    public BufferedState withDepth(int depth) {
        return new BufferedState(queue, segments, buffer, depth);
    }

    public Optional<Character> peek() {
        if (queue.isEmpty()) return Optional.empty();
        return Optional.of(queue.peek());
    }

    public Optional<Tuple<BufferedState, Character>> popAndAppend() {
        return pop().map(tuple -> new Tuple<>(tuple.left().append(tuple.right()), tuple.right()));
    }

    public Stream<String> stream() {
        return new JavaList<>(segments).stream();
    }

    public Optional<BufferedState> popAndAppendDiscard() {
        return popAndAppend().map(Tuple::left);
    }

    public boolean isShallow() {
        return depth == 1;
    }

    public BufferedState exit() {
        return new BufferedState(queue, segments, buffer, depth - 1);
    }

    public BufferedState enter() {
        return new BufferedState(queue, segments, buffer, depth + 1);
    }
}
