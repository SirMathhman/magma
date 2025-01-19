package magma.locate;

import java.util.Optional;

public record LastLocator(String infix) implements Locator {
    @Override
    public Optional<Integer> locate(String input) {
        final var index = input.lastIndexOf(infix());
        return index == -1 ? Optional.empty() : Optional.of(index);
    }

    @Override
    public String unwrap() {
        return this.infix;
    }

    @Override
    public int length() {
        return this.infix.length();
    }
}