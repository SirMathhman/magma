package magma.app.compile.rule;

import magma.api.java.MutableJavaList;
import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.error.CompileError;
import magma.app.error.FormattedError;
import magma.app.error.NodeContext;
import magma.app.error.InputContext;

import java.util.List;

public record OrRule(List<Rule> rules) implements Rule {
    @Override
    public Result<Node, FormattedError> parse(Input input) {
        var errors = new MutableJavaList<FormattedError>();
        for (Rule rule : rules) {
            final var parsed = rule.parse(new Input(input.input()));
            if (parsed.isOk()) return parsed;
            errors.add(parsed.findError().orElseNull());
        }

        return new Err<>(new CompileError("Invalid input", new InputContext(new Input(input.input())), errors));
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        var errors = new MutableJavaList<FormattedError>();
        for (Rule rule : rules) {
            final var generated = rule.generate(node);
            if (generated.isOk()) return generated;
            errors.add(generated.findError().orElseNull());
        }

        return new Err<>(new CompileError("Invalid node", new NodeContext(node), errors));
    }
}
