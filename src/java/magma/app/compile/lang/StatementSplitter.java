package magma.app.compile.lang;

import magma.app.compile.rule.Splitter;
import magma.java.JavaCollectors;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StatementSplitter implements Splitter {
    static BufferedState splitAtChar(BufferedState state, char c) {
        return splitDoubleQuotes(state, c)
                .or(() -> splitSingleQuotes(state, c))
                .orElseGet(() -> splitOther(state, c));
    }

    private static Optional<BufferedState> splitDoubleQuotes(BufferedState state, char c) {
        if (c != '\"') return Optional.empty();

        var current = state;
        while (true) {
            final var bufferedState = splitWithinDoubleQuotes(current);
            if (bufferedState.isPresent()) {
                current = bufferedState.get();
            } else {
                break;
            }
        }

        return Optional.of(current);
    }

    private static Optional<BufferedState> splitWithinDoubleQuotes(BufferedState current) {
        final var optional = current.popAndAppend();
        if (optional.isEmpty()) return Optional.empty();

        final var next = optional.get();
        final var nextState = next.left();
        final var nextChar = next.right();
        if (nextChar == '\\') {
            return Optional.of(nextState.popAndAppendDiscard().orElse(nextState));
        }

        if (nextChar == '\"') {
            return Optional.empty();
        }

        return Optional.of(nextState);
    }

    private static Optional<BufferedState> splitSingleQuotes(BufferedState state, char c) {
        if (c != '\'') return Optional.empty();

        final var optional = state.popAndAppend();
        if (optional.isEmpty()) return Optional.of(state);

        final var next = optional.get();
        final var nextState = next.left();
        final var nextChar = next.right();

        final var escaped = splitEscapeInSingleQuotes(nextState, nextChar);

        return escaped.popAndAppendDiscard();
    }

    private static BufferedState splitEscapeInSingleQuotes(BufferedState nextState, char nextChar) {
        if (nextChar == '\\') {
            return nextState.popAndAppendDiscard().orElse(nextState);
        } else {
            return nextState;
        }
    }

    private static BufferedState splitOther(BufferedState appended, char c) {
        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '}' && appended.isShallow()) return appended.exit().advance();
        if (c == '{' || c == '(') return appended.enter();
        if (c == '}' || c == ')') return appended.exit();
        return appended;
    }

    @Override
    public List<String> split(String input) {
        final var queue = IntStream.range(0, input.length())
                .mapToObj(input::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        var state = new BufferedState(queue);
        while (true) {
            final var optional = state.popAndAppend();
            if (optional.isEmpty()) break;

            final var next = optional.get();
            state = splitAtChar(next.left(), next.right());
        }

        return state.advance()
                .stream()
                .collect(JavaCollectors.asList());
    }
}