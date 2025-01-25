package magma.app.compile.rule;

import magma.api.result.Result;
import magma.app.compile.node.Node;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;
import magma.app.error.context.StringContext;

import java.util.List;

public record ContextRule(String message, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return this.childRule.parse(input).mapErr(err -> new CompileError(this.message, new StringContext(input), List.of(err)));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return this.childRule.generate(node).mapErr(err -> new CompileError(this.message, new NodeContext(node), List.of(err)));
    }
}
