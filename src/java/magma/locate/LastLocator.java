package magma.locate;

import java.util.Optional;

public record LastLocator(String slice) implements Locator {
    @Override
    public int computeLength() {
        return slice().length();
    }

    @Override
    public Optional<Integer> locate(String input) {
        final var index = input.lastIndexOf(slice());
        if (index == -1) return Optional.empty();
        return Optional.of(index);
    }

    @Override
    public String createErrorMessage() {
        return "No slice present: '" + this.slice + "'";
    }
}