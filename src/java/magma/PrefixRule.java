package magma;

import java.util.Optional;

public record PrefixRule(String prefix, Rule childRule) implements Rule {
    private Optional<String> parse0(String segment) {
        if (!segment.startsWith(prefix())) return Optional.empty();
        final var substring = segment.substring(prefix().length());
        return this.childRule().parse(substring).map(node -> node.findValue(Compiler.VALUE).orElseThrow());
    }

    @Override
    public Optional<Node> parse(String input) {
        return parse0(input).map(value -> new Node(Optional.empty()).withString(Compiler.VALUE, value));
    }

    @Override
    public Optional<String> generate(Node node) {
        return childRule.generate(node).map(slice -> prefix + slice);
    }
}