import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.Context;
import magma.app.error.context.NodeContext;
import magma.app.error.context.StringContext;
import java.util.Optional;
public struct LazyRule implements Rule {
	private Optional<Rule> childRule=Optional.empty();
	Result<Node, CompileError> parse=Result<Node, CompileError> parse(String input){
		return findChild(new StringContext(input)).flatMapValue(rule ->rule.parse(input));
	};
	Result<Rule, CompileError> findChild=Result<Rule, CompileError> findChild(Context context){
		if(this.childRule.isPresent()){
		return new Ok<>(this.childRule.get());
	}
		else {
		return new Err<>(new CompileError("Child rule is not set.", context));
	}
	};
	Result<String, CompileError> generate=Result<String, CompileError> generate(Node node){
		return findChild(new NodeContext(node)).flatMapValue(rule ->rule.generate(node));
	};
	void set=void set(Rule childRule){
		this.childRule = Optional.of(childRule);
	};
}