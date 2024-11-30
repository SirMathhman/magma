package magma.app.compile.pass;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.Node;

public interface Passer {
    default Option<Result<Node, CompileError>> afterNode(Node node) {
        return new None<Result<Node, CompileError>>();
    }

    default Option<Result<Node, CompileError>> beforeNode(Node node) {
        return new None<Result<Node, CompileError>>();
    }
}
