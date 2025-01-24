import magma.api.result.Err;import magma.api.result.Ok;import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.Context;import magma.app.error.context.NodeContext;import magma.app.error.context.StringContext;import java.util.Optional;struct LazyRule{
	Optional<Rule> childRule=Optional.empty();
	Result<Node, CompileError> parse(any* _ref_, String input){
		return findChild(new StringContext(input)).flatMapValue(()->rule.parse(input));
	}
	Result<Rule, CompileError> findChild(any* _ref_, Context context){
		if(this.childRule.isPresent()){
			return new Ok<>(this.childRule.get());
		}
		else{
			return new Err<>(new CompileError("Child rule is not set.", context));
		}
	}
	Result<String, CompileError> generate(any* _ref_, Node node){
		return findChild(new NodeContext(node)).flatMapValue(()->rule.generate(node));
	}
	void set(any* _ref_, Rule childRule){
		this.childRule = Optional.of(childRule);
	}
	Rule N/A(){
		return N/A.new();
	}
}