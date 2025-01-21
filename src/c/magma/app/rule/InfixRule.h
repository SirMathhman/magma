import magma.api.Tuple;
import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.StringContext;
import magma.app.rule.locate.Locator;
import java.util.ArrayList;
import java.util.Optional;
final struct InfixRule implements Rule {
	const Rule leftRule;
	const Locator locator;
	const Rule rightRule;
	public InfixRule(Rule leftRule, Locator locator, Rule rightRule){
		this.leftRule =leftRule;
		this.locator =locator;
		this.rightRule =rightRule;
	}
	@Override
Result<String, CompileError> generate(Node node){
		return this.leftRule.generate(node).and(
                () ->this.rightRule.generate(node)).mapValue(Tuple.merge(
                (left, right) ->left+this.locator.unwrap() + right));
	}
	@Override
Result<Node, CompileError> parse(String input){
		const var indices=this.locator.locate(input).foldLeft(new ArrayList<>(), InfixRule::add);
		const var errors=new ArrayList<CompileError>();
		int i=0;
		while(i<indices.size()){
			int index=indices.get(i);
			const var left=input.substring(0, index);
			const var right=input.substring(index+this.locator.length());
			const var result=this.leftRule.parse(left).and(() ->this.rightRule.parse(right)).mapValue(Tuple.merge(Node::merge));
			if(result.isOk()){
				return result;
			}
			else {
				errors.add(result.findError().map(Optional::of).orElseGet(Optional::empty).orElseThrow());
			}
			i++;
		}
		return new Err<>(new CompileError("Infix '"+this.locator.unwrap() + "' not present", new StringContext(input), errors));
	}
	static ArrayList<Integer> add(ArrayList<Integer> integers, Integer integer){
		integers.add(integer);
		return integers;
	}
}
