package magma.app.compile.rule;

import magma.app.compile.GenerateException;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

public record ContextRule(String message, Rule childRule) implements Rule {
    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        return childRule.parse(input).wrapErr(new ParseException(message, input));
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        return childRule.generate(node).wrapErr(new GenerateException(message, node));
    }
}
