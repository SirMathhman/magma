package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.compile.GenerateException;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

import java.util.List;

public record OrRule(List<Rule> rules) implements Rule {
    @Override
    public Result<Node, ParseException> parse(String input) {
        for (Rule rule : rules) {
            final var parsed = rule.parse(input);
            if (parsed.isOk()) return parsed;
        }
        return new Err<>(new ParseException("Invalid input", input));
    }

    @Override
    public Result<String, GenerateException> generate(Node node) {
        for (Rule rule : rules) {
            final var generated = rule.generate(node);
            if (generated.isOk()) return generated;
        }
        return new Err<>(new GenerateException("Invalid input", node));
    }
}
