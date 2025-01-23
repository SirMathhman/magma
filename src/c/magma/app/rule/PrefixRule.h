import magma.api.result.Err;import magma.api.result.Ok;import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.StringContext;struct PrefixRule implements Rule{
	String prefix;
	Rule childRule;
	public PrefixRule(String prefix, Rule childRule){
		this.prefix =prefix;
		this.childRule =childRule;
	}
	Result<String, CompileError> truncateLeft(String input, String slice){
		if(input.startsWith(slice))return Ok<>.new();
		return Err<>.new();
	}
	Result<Node, CompileError> parse(String input){
		return truncateLeft(input, this.prefix).flatMapValue(this.childRule::parse);
	}
	Result<String, CompileError> generate(Node node){
		return this.childRule.generate(node).mapValue(()->this.prefix + inner);
	}
	struct PrefixRule new(){
		struct PrefixRule this;
		return this;
	}
}