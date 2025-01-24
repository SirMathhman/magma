import magma.api.result.Err;import magma.api.result.Ok;import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.Context;import magma.app.error.context.NodeContext;import magma.app.error.context.StringContext;import java.util.Optional;struct LazyRule implements Rule{
	Optional<Rule> childRule=Optional.empty();
	Result<Node, CompileError> parse(String input){
		return findChild(new StringContext(input)).flatMapValue(()->rule.parse(input));
	}
	Result<Rule, CompileError> findChild(Context context){
		if(this.childRule.isPresent()){
			return new Ok<>(this.childRule.get());
		}
		else{
			return new Err<>(new CompileError("Child rule is not set.", context));
		}
	}
	Result<String, CompileError> generate(Node node){
		return findChild(new NodeContext(node)).flatMapValue(()->rule.generate(node));
	}
	void set(Rule childRule){
		this.childRule = Optional.of(childRule);
	}
	Rule N/A(){
		return N/A.new();
	}
}