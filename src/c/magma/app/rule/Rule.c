import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
public struct Rule {
	((String) => Result<Node, CompileError>) parse;
	((Node) => Result<String, CompileError>) generate;
}