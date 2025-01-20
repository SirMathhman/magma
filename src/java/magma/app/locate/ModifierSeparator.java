package magma.app.locate;

import magma.app.rule.locate.Locator;

import java.util.Optional;

public class ModifierSeparator implements Locator {
    @Override
    public String unwrap() {
        return " ";
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    public Optional<Integer> locate(String input) {
        final var first = input.lastIndexOf(' ');
        if (first == -1) return Optional.empty();

        final var index = input.lastIndexOf(' ', first - 1);
        if (index == -1) return Optional.empty();

        return Optional.of(index);
    }
}
