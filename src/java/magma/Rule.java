package magma;

import magma.error.CompileError;
import magma.result.Result;

public interface Rule {
    Result<Node, CompileError> parse(String input);
}
