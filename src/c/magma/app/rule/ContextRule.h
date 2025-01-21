import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;
import magma.app.error.context.StringContext;
import java.util.List;
struct ContextRule(String message, Rule childRule) implements Rule {
	@Override
Result<Node, CompileError> parse(String input){
		return auto temp(){
			return new CompileError(this.message, new StringContext(input), List.of(err)));
		};
	}
	@Override
Result<String, CompileError> generate(Node node){
		return auto temp(){
			return new CompileError(this.message, new NodeContext(node), List.of(err)));
		};
	}
}

