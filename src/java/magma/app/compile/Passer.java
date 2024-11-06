package magma.app.compile;

import magma.api.option.Option;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;

public interface Passer {
    Option<Result<Node, CompileError>> pass(Node type);
}
