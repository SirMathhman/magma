package magma;

import java.util.Optional;

public record PrefixRule(String prefix, Rule childRule) implements Rule {
    private Optional<String> parse0(String segment) {
        if (!segment.startsWith(prefix())) return Optional.empty();
        final var substring = segment.substring(prefix().length());
        return this.childRule().parse(substring).map(Node::value);
    }

    @Override
    public Optional<Node> parse(String input) {
        return parse0(input).map(Node::new);
    }
}