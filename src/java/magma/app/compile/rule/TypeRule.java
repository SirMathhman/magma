package magma.app.compile.rule;

import magma.api.result.Err;
import magma.app.compile.GenerateException;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

public record TypeRule(String type, Rule rule) implements Rule {
    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        return rule.parse(input).mapValue(node -> node.retype(type));
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        if (!node.is(type))
            return new RuleResult<>(new Err<>(new GenerateException("Expected a type of '" + type + "'", node)));
        return rule.generate(node);
    }
}
