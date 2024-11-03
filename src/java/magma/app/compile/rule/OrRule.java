package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;
import magma.app.compile.error.StringContext;

import java.util.ArrayList;
import java.util.List;

public record OrRule(List<Rule> rules) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        var errors = new ArrayList<CompileError>();
        for (var rule : rules) {
            final var result = rule.parse(input);
            if (result.isOk()) {
                return result;
            } else {
                result.findErr().ifPresent(errors::add);
            }
        }

        return new Err<>(new CompileError("No valid rule", new StringContext(input), errors));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        var errors = new ArrayList<CompileError>();
        for (var rule : rules) {
            final var result = rule.generate(node);
            if (result.isOk()) {
                return result;
            } else {
                result.findErr().ifPresent(errors::add);
            }
        }

        return new Err<>(new CompileError("No valid rule", new NodeContext(node), errors));
    }
}
