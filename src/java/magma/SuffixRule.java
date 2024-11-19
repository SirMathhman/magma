package magma;

import java.util.Optional;

public record SuffixRule(StringRule childRule, String suffix) implements Rule {
    @Override
    public Optional<String> parse(String input) {
        if (!input.endsWith(suffix())) return Optional.empty();
        final var slice = input.substring(0, input.length() - suffix().length());
        return childRule().parse(slice);
    }
}