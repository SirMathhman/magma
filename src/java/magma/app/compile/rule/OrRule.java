package magma.app.compile.rule;

import magma.app.compile.Node;
import magma.app.compile.CompileError;
import magma.app.error.Error;
import magma.app.error.NodeContext;
import magma.app.error.StringContext;
import magma.java.JavaList;
import magma.api.result.Err;
import magma.api.result.Result;

public record OrRule(JavaList<Rule> rules) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        var causes = new JavaList<Error>();
        for (var rule : rules.list()) {
            final var parsed = rule.parse(input);
            if (parsed.isOk()) return parsed;
            causes.add(parsed.findErr().orElse(new CompileError()));
        }
        return new Err<>(new CompileError("No valid rule present", new StringContext(input), causes));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        var causes = new JavaList<Error>();
        for (var rule : rules.list()) {
            final var generated = rule.generate(node);
            if (generated.isOk()) return generated;
            causes.add(generated.findErr().orElse(new CompileError()));
        }
        return new Err<>(new CompileError("No valid rule present", new NodeContext(node), causes));
    }
}
