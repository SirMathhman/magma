import magma.api.result.Err;import magma.api.result.Ok;import magma.api.result.Result;import magma.app.MapNode;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.StringContext;struct ExactRule(String slice){
	Result<Node, CompileError> parse(any* _ref_, String input){
		if(input.equals(this.slice))return new Ok<>(new MapNode());
		var context=new StringContext(input);
		return new Err<>(new CompileError("Exact string '"+this.slice + "' was not present", context));
	}
	Result<String, CompileError> generate(any* _ref_, Node node){
		return new Ok<>(this.slice);
	}
	Rule N/A(){
		return N/A.new();
	}
}