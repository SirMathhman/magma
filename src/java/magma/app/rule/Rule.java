package magma.app.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;
import magma.app.error.context.StringContext;

public interface Rule {
    default Result<Node, CompileError> parse(String input) {
        return new Err<>(new CompileError("Cannot parse", new StringContext(input)));
    }

    default Result<String, CompileError> generate(Node node) {
        return new Err<>(new CompileError("Cannot generate", new NodeContext(node)));
    }
}
