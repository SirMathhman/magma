package magma.compile.rule;

import magma.compile.Node;
import magma.option.None;
import magma.option.Option;

public record SuffixRule(Rule childRule, String suffix) implements Rule {
    @Override
    public Option<Node> parse(String input) {
        if (!input.endsWith(suffix)) return new None<>();
        final var value = input.substring(0, input.length() - suffix.length());

        return childRule().parse(value);
    }

    @Override
    public Option<String> generate(Node node) {
        return childRule.generate(node).map(value -> value + suffix);
    }
}