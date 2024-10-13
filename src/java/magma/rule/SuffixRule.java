package magma.rule;

import magma.Node;

import java.util.Optional;

public final class SuffixRule implements Rule {
    private final String suffix;
    private final Rule childRule;

    public SuffixRule(String suffix, Rule childRule) {
        this.suffix = suffix;
        this.childRule = childRule;
    }

    @Override
    public Optional<Node> parse(String input) {
        if (!input.endsWith(suffix)) return Optional.empty();
        final var slice = input.substring(0, input.length() - suffix.length());
        return childRule.parse(slice);
    }

    @Override
    public Optional<String> generate(Node node) {
        return childRule.generate(node).map(value -> value + suffix);
    }
}