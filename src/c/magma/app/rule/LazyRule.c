import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.Context;
import magma.app.error.context.NodeContext;
import magma.app.error.context.StringContext;
import java.util.Optional;

@Override
Result<Node, CompileError> parse(String input){
	return findChild(new StringContext(input)).flatMapValue(auto _lambda33_(auto rule){
		return rule.parse(input);
	});
}

Result<Rule, CompileError> findChild(Context context){
	if(this.childRule.isPresent()){
		return new Ok<>(this.childRule.get());
	}
	else {
		return new Err<>(new CompileError("Child rule is not set.", context));
	}
}

@Override
Result<String, CompileError> generate(Node node){
	return findChild(new NodeContext(node)).flatMapValue(auto _lambda34_(auto rule){
		return rule.generate(node);
	});
}

void set(Rule childRule){
	this.childRule = Optional.of(childRule);
}
struct LazyRule implements Rule {Optional<Rule> childRule=Optional.empty();
}

