package magma.locate;

import magma.Tuple;

import java.util.Optional;
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
            String input,
            Tuple<Optional<Integer>, Integer> current,
            int index
    ) {
        final var found = current.left();
        if (found.isPresent()) return current;

        final var depth = current.right();
        final var c = input.charAt(index);
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
        return IntStream.range(0, input.length())
                .mapToObj(index -> input.length() - 1 - index)
                .reduce(new Tuple<>(Optional.<Integer>empty(), 0),
                        (current, tuple) -> fold(input, current, tuple),
                        (_, next) -> next).left();
    }
}
