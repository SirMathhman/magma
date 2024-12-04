package magma.compile.rule;

import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.NodeContext;
import magma.compile.error.StringContext;
import magma.api.result.Err;
import magma.api.result.Result;

import java.util.List;

public record OrRule(List<Rule> rules) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        for (Rule rule : rules) {
            final var parsed = rule.parse(input);
            if (parsed.isOk()) return parsed;
        }

        return new Err<>(new CompileError("No valid combination", new StringContext(input)));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        for (Rule rule : rules) {
            final var generated = rule.generate(node);
            if (generated.isOk()) return generated;
        }

        return new Err<>(new CompileError("No valid combination", new NodeContext(node)));
    }
}
