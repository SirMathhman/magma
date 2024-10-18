package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.compile.GenerateException;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

import java.util.List;

public record OrRule(List<Rule> rules) implements Rule {
    private Result<Node, ParseException> parse2(String input) {
        for (Rule rule : rules) {
            final var parsed = rule.parse(input).unwrap();
            if (parsed.isOk()) return parsed;
        }
        return new Err<>(new ParseException("Invalid input", input));
    }

    private Result<String, GenerateException> generate2(Node node) {
        for (Rule rule : rules) {
            final var generated = rule.generate(node).unwrap();
            if (generated.isOk()) return generated;
        }
        return new Err<>(new GenerateException("Invalid input", node));
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
