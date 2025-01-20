import magma.api.Tuple;
import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.StringContext;
import magma.app.rule.locate.Locator;
import java.util.ArrayList;
public final struct InfixRule implements Rule {
	private final Rule leftRule;
	private final Locator locator;
	private final Rule rightRule;
	public InfixRule(Rule leftRule, Locator locator, Rule rightRule){
		this.leftRule =leftRule;
		this.locator =locator;
		this.rightRule =rightRule;
	}
	@Override
public Result<String, CompileError> generate(Node node){
		return this.leftRule.generate(node).and(
                () ->this.rightRule.generate(node)).mapValue(Tuple.merge(
                (left, right) ->left+this.locator.unwrap() + right));
	}
	@Override
public Result<Node, CompileError> parse(String input){
		final var indices=this.locator.locate(input).foldLeft(new ArrayList<>(), InfixRule::add);
		final var errors=new ArrayList<CompileError>();
		int i=0;
		while(i<indices.size()){
		int index=indices.get(i);
		final var left=input.substring(0, index);
		final var right=input.substring(index+this.locator.length());
		final var result=this.leftRule.parse(left).and(() ->this.rightRule.parse(right)).mapValue(Tuple.merge(Node::merge));
		if(result.isOk()){
		return result;
	}
		else {
		errors.add(result.findError().orElseThrow());
	}
		i++;
	}
		return new Err<>(new CompileError("Infix '"+this.locator.unwrap() + "' not present", new StringContext(input), errors));
	}
	private static ArrayList<Integer> add(ArrayList<Integer> integers, Integer integer){
		integers.add(integer);
		return integers;
	}
}