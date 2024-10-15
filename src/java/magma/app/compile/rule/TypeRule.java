package magma.app.compile.rule;

import magma.app.compile.GenerateException;
import magma.app.compile.Node;
import magma.app.compile.ParseException;
import magma.api.result.Err;
import magma.api.result.Result;

import java.util.Collections;

public record TypeRule(String type, Rule rule) implements Rule {
    private Result<Node, ParseException> parse1(String input) {
        return rule.parse(input).first().orElseThrow().mapValue(node -> node.retype(type));
    }

    private Result<String, GenerateException> generate1(Node node) {
        if (!node.is(type)) return new Err<>(new GenerateException("Expected a type of '" + type + "'", node));
        return rule.generate(node).first().orElseThrow();
    }

    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        return new RuleResult<>(Collections.singletonList(parse1(input)));
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        return new RuleResult<>(Collections.singletonList(generate1(node)));
    }
}
