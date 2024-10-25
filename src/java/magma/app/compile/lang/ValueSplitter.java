package magma.app.compile.lang;

import magma.api.Tuple;
import magma.app.compile.rule.Splitter;
import magma.java.NativeListCollector;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ValueSplitter implements Splitter {

    private static BufferedState splitAtChar(BufferedState current, Character c) {
        return processDoubleQuotes(current, c)
                .or(() -> processMinusSign(current, c))
                .or(() -> processComma(current, c))
                .orElseGet(() -> processNormal(current, c));
    }

    private static BufferedState processNormal(BufferedState current, Character c) {
        final var appended = current.append(c);
        if (c == '<' || c == '(') return appended.withDepth(current.depth() + 1);
        if (c == '>' || c == ')') return appended.withDepth(current.depth() - 1);
        return appended;
    }

    private static Optional<BufferedState> processComma(BufferedState current, char c) {
        if (c == ',' && current.isLevel()) {
            return Optional.of(current.advance());
        } else {
            return Optional.empty();
        }
    }

    private static Optional<BufferedState> processMinusSign(BufferedState current, char c) {
        if (c != '-') return Optional.empty();

        final var appended = current.append('-');
        final var peeked = appended.peek();
        if (peeked.isEmpty() || peeked.get() != '>') return Optional.of(appended);

        return appended.popAndAppend().map(Tuple::left);
    }

    private static Optional<BufferedState> processDoubleQuotes(BufferedState state, char c) {
        if (c != '\"') return Optional.empty();

        var current = state.append(c);
        while (true) {
            var processed = processInDoubleQuotes(current);
            if (processed.isEmpty()) return Optional.of(current);
            current = processed.get();
        }
    }

    private static Optional<BufferedState> processInDoubleQuotes(BufferedState current) {
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
        }

        if (nextChar == '\"') {
            return Optional.empty();
        }

        return Optional.of(nextState);
    }

    @Override
    public List<String> split(String input) {
        final var length = input.length();
        var queue = IntStream.range(0, length)
                .mapToObj(input::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        var current = new BufferedState(queue);
        while (true) {
            final var popped = current.pop();
            if (popped.isEmpty()) break;

            var c = popped.get().right();
            current = splitAtChar(current, c);
        }

        return current.advance()
                .stream()
                .collect(new NativeListCollector<String>());
    }

    @Override
    public StringBuilder append(StringBuilder buffer, String str) {
        if (buffer.isEmpty()) return buffer.append(str);
        return buffer.append(", ").append(str);
    }
}
