package magma.locate;

import magma.Tuple;

import java.util.Optional;
import java.util.stream.IntStream;

public class TypeLocator implements Locator {
    private static Tuple<Optional<Integer>, Integer> fold(
            String input,
            Tuple<Optional<Integer>, Integer> current,
            int index
    ) {
        final var found = current.left();
        if (found.isPresent()) return current;

        final var depth = current.right();
        final var c = input.charAt(index);
        if (c == ' ' && depth == 0) return new Tuple<>(Optional.of(index), depth);
        if (c == '>') return new Tuple<>(Optional.empty(), depth + 1);
        if (c == '<') return new Tuple<>(Optional.empty(), depth - 1);
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
