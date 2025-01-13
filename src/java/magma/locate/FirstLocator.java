package magma.locate;

import java.util.Optional;

public record FirstLocator(String slice) implements Locator {
    @Override
    public int computeLength() {
        return slice().length();
    }

    @Override
    public Optional<Integer> locate(String input) {
        final var index = input.indexOf(slice());
        if (index == -1) return Optional.empty();
        return Optional.of(index);
    }
}