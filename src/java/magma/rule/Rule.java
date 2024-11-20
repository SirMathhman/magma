package magma.rule;

import magma.CompileException;
import magma.Node;
import magma.result.Result;

public interface Rule {
    Result<Node, CompileException> parse(String input);

    Result<String, CompileException> generate(Node node);
}
