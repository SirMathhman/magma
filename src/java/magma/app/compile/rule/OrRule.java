package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.Node;
import magma.api.error.Error;
import magma.app.compile.error.NodeContext;
import magma.app.compile.error.StringContext;
import magma.java.JavaList;

public record OrRule(JavaList<Rule> rules) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        var causes = new JavaList<Error>();
        for (var rule : rules.list()) {
            final var parsed = rule.parse(input);
            if (parsed.isOk()) return parsed;
            causes = causes.add(parsed.findErr().orElse(new CompileError()));
        }
        return new Err<>(new CompileError("No valid rule present", new StringContext(input), causes));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        var causes = new JavaList<Error>();
        for (var rule : rules.list()) {
            final var generated = rule.generate(node);
            if (generated.isOk()) return generated;
            causes = causes.add(generated.findErr().orElse(new CompileError()));
        }
        return new Err<>(new CompileError("No valid rule present", new NodeContext(node), causes));
    }
}
