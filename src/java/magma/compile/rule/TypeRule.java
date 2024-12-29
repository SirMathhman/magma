package magma.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.NodeContext;
import magma.compile.error.StringContext;

import java.util.Collections;

public record TypeRule(String type, Rule childRule) implements Rule {
    static int depth = -1;

    @Override
    public Result<Node, CompileError> parse(String input) {
        final var parsed = childRule.parse(input);
        return parsed.mapValue(node -> node.retype(type))
                .mapErr(err -> new CompileError("Cannot assign type '" + type + "'", new StringContext(input), Collections.singletonList(err)));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        if (!node.is(type))
            return new Err<>(new CompileError("Type '" + type + "' not present", new NodeContext(node)));
        return childRule.generate(node).mapErr(err -> new CompileError("Cannot assign maybeType '" + type + "'", new NodeContext(node), Collections.singletonList(err)));
    }
}
