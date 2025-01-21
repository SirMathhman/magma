import magma.api.Tuple;
import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.StringContext;
import magma.app.rule.locate.Locator;
import java.util.ArrayList;
import java.util.Optional;
struct InfixRule implements Rule {
	 Rule leftRule;
	 Locator locator;
	 Rule rightRule;
	public InfixRule(Rule leftRule, Locator locator, Rule rightRule){
		this.leftRule =leftRule;
		this.locator =locator;
		this.rightRule =rightRule;
	}
	@Override
Result<String, CompileError> generate(Node node){
		return this.leftRule.generate(node).and(auto _lambda25_(){
			return this.rightRule.generate(node);
		}).mapValue(Tuple.merge(
                (left, right) -> left + this.locator.unwrap() + right));
	}
	@Override
Result<Node, CompileError> parse(String input){
		 auto indices=this.locator.locate(input).foldLeft(new ArrayList<>(), InfixRule.add);
		 auto errors=new ArrayList<CompileError>();
		int i=0;
		while(i<indices.size()){
			int index=indices.get(i);
			 auto left=input.substring(0, index);
			 auto right=input.substring(index+this.locator.length());
			 auto result=this.leftRule.parse(left).and(auto _lambda26_(){
				return this.rightRule.parse(right);
			}).mapValue(Tuple.merge(Node.merge));
			if(result.isOk()){
				return result;
			}
			else {
				errors.add(result.findError().map(Optional.of).orElseGet(Optional.empty).orElseThrow());
			}
			i++;
		}
		return new Err<>(new CompileError("Infix '"+this.locator.unwrap() + "' not present", new StringContext(input), errors));
	}
	 ArrayList<Integer> add(ArrayList<Integer> integers, Integer integer){
		integers.add(integer);
		return integers;
	}
}
