package magma.rule;

import magma.Node;

import java.util.Optional;

public record SuffixRule(StringRule childRule, String suffix) implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        if (!input.endsWith(this.suffix())) return Optional.empty();
        final var slice = input.substring(0, input.length() - this.suffix().length());
        return this.childRule().parse(slice);
    }

    @Override
    public Optional<String> generate(Node node) {
        return childRule.generate(node).map(slice -> slice + suffix);
    }
}