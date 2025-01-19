package magma.app.rule;

import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;

import java.util.function.Function;

public interface Rule {
    Result<Node, CompileError> apply(String s);
}
