package magma;

import java.util.Optional;

public record PrefixRule(String prefix, Rule mapper) implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        if (!input.startsWith(prefix())) return Optional.empty();
        final var slice = input.substring(prefix().length());
        return mapper().parse(slice);
    }
}