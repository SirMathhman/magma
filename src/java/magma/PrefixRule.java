package magma;

import java.util.Optional;

public record PrefixRule(String prefix, Rule rule) implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        if (!input.startsWith(prefix())) return Optional.empty();
        final var withoutKeyword = input.substring(prefix().length());
        return rule().parse(withoutKeyword);
    }

    @Override
    public Optional<String> generate(Node node) {
        return rule.generate(node).map(inner -> prefix + inner);
    }
}