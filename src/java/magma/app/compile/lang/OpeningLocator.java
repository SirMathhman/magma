package magma.app.compile.lang;

import magma.api.Tuple;
import magma.app.compile.rule.Locator;

import java.util.Optional;
import java.util.stream.Stream;

class OpeningLocator implements Locator {
    @Override
    public Stream<Integer> locate(String input) {
        var depth = 0;
        int i = input.length() - 1;
        while (i >= 0) {
            var c = input.charAt(i);

            final var tuple = locateAtIndex(depth, i, c);
            depth = tuple.left();

            final var right = tuple.right();
            if (right.isPresent()) {
                return right.get();
            }

            i--;
        }

        return Stream.empty();
    }

    private Tuple<Integer, Optional<Stream<Integer>>> locateAtIndex(int depth, int index, char c) {
        if (c == '(' && depth == 1) {
            return new Tuple<>(depth, Optional.of(Stream.of(index)));
        } else {
            if (c == ')') return new Tuple<>(depth + 1, Optional.empty());
            if (c == '(') return new Tuple<>(depth - 1, Optional.empty());
            return new Tuple<>(depth, Optional.empty());
        }
    }

    @Override
    public String slice() {
        return "(";
    }
}
