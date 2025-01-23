import magma.api.result.Err;import magma.api.result.Ok;import magma.api.result.Result;import magma.app.MapNode;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.NodeContext;struct StringRule implements Rule{
	String propertyKey;
	public StringRule(String propertyKey){
		this.propertyKey =propertyKey;
	}
	Result<String, CompileError> parse(Node node){
		return node.findString(this.propertyKey).<Result<String, CompileError>>map(Ok::new).orElseGet(()->Err<>.new());
	}
	Result<Node, CompileError> parse(String input){
		return Ok<>.new();
	}
	Result<String, CompileError> generate(Node node){
		return node.findString(this.propertyKey).<Result<String, CompileError>>map(Ok::new).orElseGet(()->Err<>.new());
	}struct StringRule implements Rule new(){struct StringRule implements Rule this;return this;}
}