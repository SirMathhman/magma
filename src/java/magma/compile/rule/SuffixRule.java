package magma.compile.rule;

import magma.compile.Node;
import magma.core.String_;
import magma.core.option.Option;

public record SuffixRule(Rule childRule, String suffix) implements Rule {
    @Override
    public Option<String_> generate(Node node) {
        return childRule().generate(node).map(inner -> inner.appendSlice(suffix()));
    }

    @Override
    public Option<Node> parse(String_ input) {
        return input.truncateLeftBySlice(suffix()).flatMap(childRule()::parse);
    }
}