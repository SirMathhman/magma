package magma.app.compile.rule;

import java.util.Optional;
import java.util.stream.Stream;

public record FirstLocator(String slice) implements Locator {
    private Optional<Integer> locate0(String input) {
        final var index = input.indexOf(slice);
        return index == -1 ? Optional.empty() : Optional.of(index);
    }

    @Override
    public Stream<Integer> locate(String input) {
        return locate0(input).stream();
    }
}