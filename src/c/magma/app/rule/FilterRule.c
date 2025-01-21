import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.StringContext;
import java.util.function.Predicate;

@Override
Result<Node, CompileError> parse(String input){
	if(this.filter.test(input)){
		return this.childRule.parse(input);
	}
	else {
		return new Err<>(new CompileError("Filter did not apply", new StringContext(input)));
	}
}

@Override
Result<String, CompileError> generate(Node node){
	return this.childRule.generate(node);
}
struct FilterRule(Predicate<String> filter,
                         Rule childRule) implements Rule {
}

