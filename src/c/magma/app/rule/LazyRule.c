import magma.api.result.Err;import magma.api.result.Ok;import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.Context;import magma.app.error.context.NodeContext;import magma.app.error.context.StringContext;import java.util.Optional;struct LazyRule implements Rule{
	Optional<Rule> childRule=Optional.empty();
	Result<Node, CompileError> parse(String input){
		return findChild(StringContext.new()).flatMapValue(()->rule.parse(input));
	}
	Result<Rule, CompileError> findChild(Context context){
		if(this.childRule.isPresent()){
			return Ok<>.new();
		}
		else{
			return Err<>.new();
		}
	}
	Result<String, CompileError> generate(Node node){
		return findChild(NodeContext.new()).flatMapValue(()->rule.generate(node));
	}
	void set(Rule childRule){
		this.childRule = Optional.of(childRule);
	}
}