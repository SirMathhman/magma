import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
public interface Rule {Result<Node, CompileError> parse(String input);Result<String, CompileError> generate(Node node);}