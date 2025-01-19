package magma.app.rule;

import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;

public interface Rule {
    default Result<Node, CompileError> parse(String s) {
        throw new UnsupportedOperationException();
    }

    default Result<String, CompileError> generate(Node node) {
        throw new UnsupportedOperationException();
    }
}
