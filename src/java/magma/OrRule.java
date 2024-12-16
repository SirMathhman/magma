package magma;

import magma.error.CompileError;
import magma.result.Err;
import magma.result.Result;

import java.util.List;

public record OrRule(List<Rule> rules) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        for (Rule rule : rules) {
            final var parsed = rule.parse(input);
            if (parsed.isOk()) return parsed;
        }

        return new Err<>(new CompileError("Invalid input", new StringContext(input)));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        for (Rule rule : rules) {
            final var generated = rule.generate(node);
            if (generated.isOk()) return generated;
        }

        return new Err<>(new CompileError("Invalid node", new NodeContext(node)));
    }
}
