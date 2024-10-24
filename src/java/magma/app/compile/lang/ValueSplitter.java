package magma.app.compile.lang;

import magma.api.Tuple;
import magma.app.compile.rule.Splitter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ValueSplitter implements Splitter {

    private static State splitAtChar(State current, Character c) {
        return processDoubleQuotes(current, c)
                .or(() -> processMinusSign(current, c))
                .or(() -> processComma(current, c))
                .orElseGet(() -> processNormal(current, c));
    }

    private static State processNormal(State current, Character c) {
        final var appended = current.append(c);
        if (c == '<' || c == '(') return appended.withDepth(current.depth() + 1);
        if (c == '>' || c == ')') return appended.withDepth(current.depth() - 1);
        return appended;
    }

    private static Optional<State> processComma(State current, char c) {
        if (c == ',' && current.isLevel()) {
            return Optional.of(current.advance());
        } else {
            return Optional.empty();
        }
    }

    private static Optional<State> processMinusSign(State current, char c) {
        if (c != '-') return Optional.empty();

        final var appended = current.append('-');
        final var peeked = appended.peek();
        if (peeked.isEmpty() || peeked.get() != '>') return Optional.of(appended);

        return appended.popAndAppend().map(Tuple::left);
    }

    private static Optional<State> processDoubleQuotes(State state, char c) {
        if (c != '\"') return Optional.empty();

        var current = state.append(c);
        while (true) {
            var processed = processInDoubleQuotes(current);
            if (processed.isEmpty()) return Optional.of(current);
            current = processed.get();
        }
    }

    private static Optional<State> processInDoubleQuotes(State current) {
        final var optional = current.popAndAppend();
        if (optional.isEmpty()) return Optional.empty();

        final var next = optional.get();
        final var nextState = next.left();
        final var nextChar = next.right();

        if (nextChar == '\\') {
            return Optional.of(nextState
                    .popAndAppend()
                    .map(Tuple::left)
                    .orElse(nextState));
        } else if (nextChar == '\"') {
            return Optional.empty();
        } else {
            return Optional.of(nextState);
        }
    }

    @Override
    public List<String> split(String input) {
        final var length = input.length();
        var queue = IntStream.range(0, length)
                .mapToObj(input::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        var current = new State(queue);
        while (true) {
            final var popped = current.pop();
            if (popped.isEmpty()) break;

            var c = popped.get().right();
            current = splitAtChar(current, c);
        }

        return current.advance().lines;
    }

    private record State(Deque<Character> queue, List<String> lines, StringBuilder buffer, int depth) {
        public State(Deque<Character> queue) {
            this(queue, new ArrayList<>(), new StringBuilder(), 0);
        }

        private boolean isLevel() {
            return depth() == 0;
        }

        private State advance() {
            if (buffer.isEmpty()) return this;

            final var copy = new ArrayList<>(lines);
            copy.add(buffer.toString());
            return new State(queue, copy, new StringBuilder(), depth);
        }

        private Optional<Tuple<State, Character>> pop() {
            if (queue.isEmpty()) return Optional.empty();
            return Optional.of(new Tuple<>(this, queue.pop()));
        }

        private State append(Character c) {
            buffer().append(c);
            return this;
        }

        public State withDepth(int depth) {
            return new State(queue, lines, buffer, depth);
        }

        public Optional<Character> peek() {
            if (queue.isEmpty()) return Optional.empty();
            return Optional.of(queue.peek());
        }

        public Optional<Tuple<State, Character>> popAndAppend() {
            return pop().map(tuple -> new Tuple<>(tuple.left().append(tuple.right()), tuple.right()));
        }
    }
}
