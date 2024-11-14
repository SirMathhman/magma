package magma.app.compile.rule;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;

public record LastLocator(String slice) implements Locator {
    @Override
    public Option<Integer> locate(String input) {
        final var index = input.lastIndexOf(slice());
        return index == -1
                ? new None<>()
                : new Some<>(index);
    }
}