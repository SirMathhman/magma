import magma.api.result.Err;import magma.api.result.Ok;import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.StringContext;struct SuffixRule{
	String suffix;
	Rule childRule;
	public SuffixRule(any* _ref_, Rule childRule, String suffix){
		this.suffix =suffix;
		this.childRule =childRule;
	}
	Result<String, CompileError> truncateRight(any* _ref_, String input, String slice){
		if(input.endsWith(slice)){
			return new Ok<>(input.substring(0, input.length() - slice.length()));
		}
		else{
			return new Err<>(new CompileError("Suffix '"+slice+"' not present", new StringContext(input)));
		}
	}
	Result<Node, CompileError> parse(any* _ref_, String input){
		return truncateRight(input, this.suffix).flatMapValue(this.childRule::parse);
	}
	Result<String, CompileError> generate(any* _ref_, Node node){
		return childRule.generate(node).mapValue(()->inner+suffix);
	}
	Rule N/A(){
		return N/A.new();
	}
}