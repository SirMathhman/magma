package magma.compile.rule;

import java.util.Optional;

public class LastLocator implements Locator {
    @Override
    public Optional<Integer> locate(String input, String infix) {
        final var index = input.lastIndexOf(infix);
        return index == -1
                ? Optional.of(index)
                : Optional.empty();
    }
}