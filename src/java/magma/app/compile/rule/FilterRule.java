package magma.app.compile.rule;

import magma.api.result.Err;
import magma.app.compile.GenerateException;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

import java.util.List;

public record FilterRule(List<String> values, Rule childRule) implements Rule {
    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        return values.contains(input) ? childRule.parse(input) : new RuleResult<>(new Err<>(new ParseException("Invalid value", input)));
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        return childRule.generate(node);
    }
}
