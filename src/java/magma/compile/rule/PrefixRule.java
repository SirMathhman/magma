package magma.compile.rule;

import magma.compile.Node;
import magma.option.None;
import magma.option.Option;

public record PrefixRule(String prefix, Rule childRule) implements Rule {
    @Override
    public Option<Node> parse(String input) {
        if (!input.startsWith(prefix())) return new None<>();
        final var slice = input.substring(prefix().length());

        return childRule().parse(slice);
    }

    @Override
    public Option<String> generate(Node node) {
        return childRule.generate(node).map(value -> prefix + value);
    }
}