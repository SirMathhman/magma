package magma;

import java.util.Optional;

public record FirstLocator(String slice) implements Locator {
    @Override
    public Optional<Integer> locate(String input) {
        final var index = input.indexOf(this.slice);
        if (index == -1) return Optional.empty();
        return Optional.of(index);
    }

    @Override
    public int length() {
        return this.slice.length();
    }
}