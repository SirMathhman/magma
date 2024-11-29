package magma.app.compile.rule;

import magma.app.compile.Node;
import magma.app.compile.CompileError;
import magma.api.result.Result;

public interface Rule {
    Result<Node, CompileError> parse(String input);

    Result<String, CompileError> generate(Node node);
}
