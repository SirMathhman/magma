package magma.app.compile.rule;

import magma.app.compile.GenerateException;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

public record StripRule(Rule childRule, String leftPaddingKey, String rightPaddingKey) implements Rule {
    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        return childRule.parse(input.strip());
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        final var leftPadding = node.findString(leftPaddingKey).orElse("");
        final var rightPadding = node.findString(rightPaddingKey).orElse("");
        return childRule.generate(node).mapValue(inner -> leftPadding + inner + rightPadding);
    }
}
