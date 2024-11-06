package magma.app.compile;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;

public interface Passer {
    default Option<Result<Node, CompileError>> beforePass(Node node) {
        return new None<>();
    }

    default Option<Result<Node, CompileError>> afterPass(Node node) {
        return new None<>();
    }
}
