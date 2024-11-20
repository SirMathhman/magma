package magma;

import java.util.Optional;

public record PrefixRule(String prefix, Rule childRule) implements Rule {

    @Override
    public Optional<Node> parse(String input) {
        if (!input.startsWith(this.prefix())) return Optional.empty();
        final var substring = input.substring(this.prefix().length());
        return this.childRule().parse(substring);
    }

    @Override
    public Optional<String> generate(Node node) {
        return childRule.generate(node).map(slice -> prefix + slice);
    }
}