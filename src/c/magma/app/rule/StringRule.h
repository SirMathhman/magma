import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.MapNode;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;
public struct StringRule implements Rule {
	private final String propertyKey;
	((String) => public) StringRule=public StringRule(String propertyKey){
		this.propertyKey =propertyKey;
	};
	((Node) => Result<String, CompileError>) parse=Result<String, CompileError> parse(Node node){
		return node.findString(this.propertyKey).<Result<String, CompileError>>map(Ok::new).orElseGet(() ->new Err<>(new CompileError("String '"+this.propertyKey + "' not present", new NodeContext(node))));
	};
	((String) => Result<Node, CompileError>) parse=Result<Node, CompileError> parse(String input){
		return new Ok<>(new MapNode().withString(this.propertyKey, input));
	};
	((Node) => Result<String, CompileError>) generate=Result<String, CompileError> generate(Node node){
		return node.findString(this.propertyKey).<Result<String, CompileError>>map(Ok::new).orElseGet(() ->new Err<>(new CompileError("String '"+this.propertyKey + "' not present", new NodeContext(node))));
	};
}