package magma.rule;

import magma.GenerateException;
import magma.Node;
import magma.ParseException;
import magma.result.Err;
import magma.result.Result;

public record TypeRule(String type, Rule rule) implements Rule {
    @Override
    public Result<Node, ParseException> parse(String input) {
        return rule.parse(input).mapValue(node -> node.retype(type));
    }

    @Override
    public Result<String, GenerateException> generate(Node node) {
        if (!node.is(type)) return new Err<>(new GenerateException("Expected a type of '" + type + "'", node));
        return rule.generate(node);
    }
}
