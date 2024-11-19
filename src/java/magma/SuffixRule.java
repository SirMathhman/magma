package magma;

import java.util.Optional;

public record SuffixRule(StringRule childRule, String suffix) implements Rule {
    private Optional<String> parse0(String input) {
        if (!input.endsWith(suffix())) return Optional.empty();
        final var slice = input.substring(0, input.length() - suffix().length());
        return this.childRule().parse(slice).map(Node::value);
    }

    @Override
    public Optional<Node> parse(String input) {
        return parse0(input).map(value -> new Node(Optional.empty(), value));
    }

    @Override
    public Optional<String> generate(Node node) {
        return childRule.generate(node).map(slice -> slice + suffix);
    }
}