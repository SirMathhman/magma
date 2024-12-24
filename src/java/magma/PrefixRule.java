package magma;

import java.util.Optional;

public record PrefixRule(String prefix, Rule childRule) implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        if (!input.startsWith(prefix)) return Optional.empty();
        final var afterKeyword = input.substring(prefix.length());
        return childRule().parse(afterKeyword);
    }

    @Override
    public Optional<String> generate(Node node) {
        return childRule().generate(node).map(value -> prefix() + value);
    }
}