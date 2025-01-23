import magma.api.result.Err;import magma.api.result.Ok;import magma.api.result.Result;import magma.app.MapNode;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.StringContext;struct ExactRule(String slice) implements Rule{
	Result<Node, CompileError> parse(String input){
		if(input.equals(this.slice))return Ok<>.new();
		var context=StringContext.new();
		return Err<>.new();
	}
	Result<String, CompileError> generate(Node node){
		return Ok<>.new();
	}struct ExactRule new(){struct ExactRule this;return this;}
}