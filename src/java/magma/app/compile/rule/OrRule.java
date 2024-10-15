package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.compile.GenerateException;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

import java.util.Collections;
import java.util.List;

public record OrRule(List<Rule> rules) implements Rule {
    private Result<Node, ParseException> parse1(String input) {
        for (Rule rule : rules) {
            final var parsed = rule.parse(input).first().orElseThrow();
            if (parsed.isOk()) return parsed;
        }
        return new Err<>(new ParseException("Invalid input", input));
    }

    private Result<String, GenerateException> generate1(Node node) {
        for (Rule rule : rules) {
            final var generated = rule.generate(node).first().orElseThrow();
            if (generated.isOk()) return generated;
        }
        return new Err<>(new GenerateException("Invalid input", node));
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
