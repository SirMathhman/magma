package magma.app.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;
import magma.app.error.context.StringContext;

import java.util.List;

public record TypeRule(String type, Rule rule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return this.rule.parse(input)
                .mapValue(node -> node.retype(this.type))
                .mapErr(err -> new CompileError("Failed to assign type '" + this.type + "'", new StringContext(input), List.of(err)));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        if (node.is(this.type)) {
            return this.rule.generate(node);
        } else {
            return new Err<>(new CompileError("Node was not of type '" + this.type + "'", new NodeContext(node)));
        }
    }
}
