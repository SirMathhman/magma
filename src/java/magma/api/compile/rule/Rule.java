package magma.api.compile.rule;

import magma.api.compile.Node;
import magma.api.error.CompileError;
import magma.api.result.Result;

public interface Rule {
    Result<Node, CompileError> parse(String input);
}
