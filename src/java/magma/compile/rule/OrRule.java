package magma.compile.rule;

import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.NodeContext;
import magma.compile.error.StringContext;
import magma.api.result.Err;
import magma.api.result.Result;

import java.util.ArrayList;
import java.util.List;

public record OrRule(List<Rule> rules) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        var errors = new ArrayList<CompileError>();
        for (Rule rule : rules) {
            final var parsed = rule.parse(input);
            if (parsed.isOk()) return parsed;
            errors.add(parsed.findError().orElseThrow());
        }

        return new Err<>(new CompileError("No valid combination", new StringContext(input), errors));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        var errors = new ArrayList<CompileError>();
        for (Rule rule : rules) {
            final var generated = rule.generate(node);
            if (generated.isOk()) return generated;
            errors.add(generated.findError().orElseThrow());
        }

        return new Err<>(new CompileError("No valid combination", new NodeContext(node), errors));
    }
}
