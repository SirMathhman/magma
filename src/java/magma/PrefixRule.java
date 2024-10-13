package magma;

import java.util.Optional;

public record PrefixRule(String prefix, Rule childRule) implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        if (!input.startsWith(prefix)) return Optional.empty();
        return childRule.parse(input.substring(prefix.length()));
    }

    @Override
    public Optional<String> generate(Node node) {
        return childRule.generate(node).map(value -> prefix + value);
    }
}