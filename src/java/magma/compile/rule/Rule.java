package magma.compile.rule;

import magma.compile.CompileError;
import magma.compile.Node;
import magma.core.String_;
import magma.core.result.Result;

public interface Rule {
    Result<Node, CompileError> parse(String_ input);

    Result<String_, CompileError> generate(Node node);
}
