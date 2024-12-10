package magma;

import java.util.Optional;

public record SuffixRule(Rule childRule, String suffix) implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        if (!input.endsWith(suffix())) return Optional.empty();
        final var slice = input.substring(0, input.length() - suffix().length());
        return childRule().parse(slice);
    }
}