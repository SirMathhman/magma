package magma.locate;

import java.util.Optional;

public class TypeLocator implements Locator {
    @Override
    public int computeLength() {
        return 1;
    }

    @Override
    public Optional<Integer> locate(String input) {
        var depth = 0;
        for (int i = input.length() - 1; i >= 0; i--) {
            final var c = input.charAt(i);
            if (c == ' ' && depth == 0) return Optional.of(i);
            if (c == '>') depth++;
            if (c == '<') depth--;
        }
        return Optional.empty();
    }
}
