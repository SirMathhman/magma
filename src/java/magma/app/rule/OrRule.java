package magma.app.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;
import magma.app.error.context.StringContext;

import java.util.ArrayList;
import java.util.List;

public record OrRule(List<Rule> rules) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String value) {
        final var errors = new ArrayList<CompileError>();
        for (Rule rule : this.rules) {
            final var result = rule.parse(value);
            if (result.isOk()) return result;
            errors.add(result.findError().orElseThrow());
        }

        return new Err<>(new CompileError("No valid rule present", new StringContext(value), errors));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        final var errors = new ArrayList<CompileError>();
        for (Rule rule : this.rules) {
            final var result = rule.generate(node);
            if (result.isOk()) return result;
            errors.add(result.findError().orElseThrow());
        }

        return new Err<>(new CompileError("No valid rule present", new NodeContext(node), errors));
    }
}
