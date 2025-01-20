import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.MapNode;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.StringContext;
public struct ExactRule(String slice) implements Rule {
	@Override
    public Result<Node, CompileError> parse(String input);
	@Override
    public Result<String, CompileError> generate(Node node){
		return new Ok<>(this.slice);
	}
}