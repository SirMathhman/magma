package magma.app.compile.rule;

import java.util.Optional;

public record FirstLocator(String slice) implements Locator {
    @Override
    public Optional<Integer> locate(String input) {
        final var index = input.indexOf(slice);
        return index == -1 ? Optional.empty() : Optional.of(index);
    }
}