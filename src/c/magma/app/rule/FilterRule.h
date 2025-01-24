import magma.api.result.Err;import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.StringContext;struct FilterRule(Predicate<String> filter, Rule childRule){
	Result<Node, CompileError> parse(any* _ref_, String input){
		if(this.filter.test(input)){
			return this.childRule.parse(input);
		}
		else{
			return new Err<>(new CompileError("Filter did not apply", new StringContext(input)));
		}
	}
	Result<String, CompileError> generate(any* _ref_, Node node){
		return this.childRule.generate(node);
	}
	Rule N/A(){
		return N/A.new();
	}
}