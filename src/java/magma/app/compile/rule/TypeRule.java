package magma.app.compile.rule;

import magma.app.Input;
import magma.app.compile.Node;
import magma.app.error.CompileError;
import magma.app.error.FormattedError;
import magma.app.error.NodeContext;
import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.error.InputContext;

public record TypeRule(String type, Rule childRule) implements Rule {
    private Result<Node, FormattedError> parse0(String input) {

        return childRule.parse(new Input(input, 0, input.length()))
                .mapValue(node -> node.retype(type))
                .mapErr(err -> new CompileError("Failed to assign type '" + type + "'", new InputContext(new Input(input, 0, input.length())), err));
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        if (node.is(type)) return childRule.generate(node);
        return new Err<>(new CompileError("Node not of type '" + type + "'", new NodeContext(node)));
    }

    @Override
    public Result<Node, FormattedError> parse(Input input) {
        return parse0(input.slice());
    }
}
