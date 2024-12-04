package magma.compile.rule;

import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.api.result.Result;

public interface Rule {
    Result<Node, CompileError> parse(String input);

    Result<String, CompileError> generate(Node node);
}
