package magma.compile.rule;

import magma.compile.Node;
import magma.core.String_;
import magma.core.option.Option;

public record PrefixRule(String prefix, Rule childRule) implements Rule {
    @Override
    public Option<Node> parse(String_ input) {
        return input.truncateLeftBySlice(prefix())
                .flatMap(childRule()::parse);
    }

    @Override
    public Option<String_> generate(Node node) {
        return childRule().generate(node)
                .map(output -> output.prependSlice(prefix()));
    }
}