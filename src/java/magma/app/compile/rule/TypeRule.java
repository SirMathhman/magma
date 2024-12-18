package magma.app.compile.rule;

import magma.api.java.MutableJavaList;
import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.error.*;

public record TypeRule(String type, Rule childRule) implements Rule {
    @Override
    public Result<Node, FormattedError> parse(Input input) {
        return childRule.parse(input)
                .mapValue(node -> node.retype(type))
                .mapErr(err -> {
                    final var context = new InputContext(input);
                    final var detail = new ContextDetail("Failed to assign type '" + type + "'", context);
                    return new CompileError(detail, err);
                });
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        if (node.is(type)) return childRule.generate(node);
        return new Err<>(new CompileError(new ContextDetail("Node not of type '" + type + "'", new NodeContext(node)), new MutableJavaList<>()));
    }
}
