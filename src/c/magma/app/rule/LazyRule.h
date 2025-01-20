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
	private Optional<Rule> childRule =Optional.empty();
	@Override
    public Result<Node, CompileError> parse(String input){return findChild(new StringContext(input)).flatMapValue(rule ->rule.parse(input));}
	private Result<Rule, CompileError> findChild(Context context){if(this.childRule.isPresent()){return new Ok<>(this.childRule.get());}else {return new Err<>(new CompileError("Child rule is not set.", context));}}
	@Override
    public Result<String, CompileError> generate(Node node){return findChild(new NodeContext(node)).flatMapValue(rule ->rule.generate(node));}
	public void set(Rule childRule){this.childRule = Optional.of(childRule);}
}