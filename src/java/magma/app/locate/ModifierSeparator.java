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
        int seen = 0;
        int depth = 0;
        int i = input.length() - 1;
        while (i >= 0) {
            var c = input.charAt(i);
            if (c == ' ' && depth == 0) seen++;
            if (seen == 2) return Optional.of(i);
            if (c == '>') depth++;
            if (c == '<') depth--;
            i--;
        }

        return Optional.empty();
    }
}
