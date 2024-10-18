package magma.app.compile.rule;

import magma.app.compile.GenerateException;
import magma.app.compile.Node;
import magma.app.compile.ParseException;
import magma.api.result.Err;
import magma.api.result.Result;

public record TypeRule(String type, Rule rule) implements Rule {
    private Result<Node, ParseException> parse2(String input) {
        return rule.parse(input).unwrap().mapValue(node -> node.retype(type));
    }

    private Result<String, GenerateException> generate2(Node node) {
        if (!node.is(type)) return new Err<>(new GenerateException("Expected a type of '" + type + "'", node));
        return rule.generate(node).unwrap();
    }

    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        return new RuleResult<>(parse2(input));
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        return new RuleResult<>(generate2(node));
    }
}
