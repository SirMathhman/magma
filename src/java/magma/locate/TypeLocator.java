package magma.locate;

import magma.Tuple;

import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TypeLocator implements Locator {
    private final char search;
    private final char enter;
    private final char exit;

    public TypeLocator(char search, char enter, char exit) {
        this.search = search;
        this.enter = enter;
        this.exit = exit;
    }

    private Tuple<Optional<Integer>, Integer> fold(
            Tuple<Integer, Character> input,
            Tuple<Optional<Integer>, Integer> current,
            LinkedList<Tuple<Integer, Character>> queue) {
        final var found = current.left();
        if (found.isPresent()) return current;

        final var depth = current.right();
        final var index = input.left();
        final var c = input.right();

        if (c == '\'') {
            queue.pop();
            if(!queue.isEmpty() && queue.peek().right() == '\\') {
                queue.pop();
            }

            queue.pop();
        }

        if (c == '"') {
            while (!queue.isEmpty()) {
                final var next = queue.pop().right();
                if (next == '"') break;

                if (!queue.isEmpty() && queue.peek().right() == '\\') {
                    queue.pop();
                }
            }

            return new Tuple<>(Optional.empty(), depth);
        }

        if (c == this.search && depth == 0) return new Tuple<>(Optional.of(index), depth);
        if (c == this.enter) return new Tuple<>(Optional.empty(), depth + 1);
        if (c == this.exit) return new Tuple<>(Optional.empty(), depth - 1);
        return new Tuple<>(Optional.empty(), depth);
    }

    @Override
    public int computeLength() {
        return 1;
    }

    @Override
    public Optional<Integer> locate(String input) {
        final var queue = IntStream.range(0, input.length())
                .mapToObj(index -> input.length() - 1 - index)
                .map(index -> new Tuple<>(index, input.charAt(index)))
                .collect(Collectors.toCollection(LinkedList::new));

        var state = new Tuple<Optional<Integer>, Integer>(Optional.empty(), 0);
        while (!queue.isEmpty()) {
            final var c = queue.pop();
            state = fold(c, state, queue);
        }

        return state.left();
    }

    @Override
    public String createErrorMessage() {
        return "No space present";
    }
}
