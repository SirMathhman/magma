import magma.api.result.Err;import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.StringContext;import magma.app.rule.locate.Locator;import java.util.ArrayList;import java.util.Optional;struct InfixRule implements Rule{
	Rule leftRule;
	Locator locator;
	Rule rightRule;
	public InfixRule(Rule leftRule, Locator locator, Rule rightRule){
		this.leftRule =leftRule;
		this.locator =locator;
		this.rightRule =rightRule;
	}
	ArrayList<Integer> add(ArrayList<Integer> integers, Integer integer){
		integers.add(integer);
		return integers;
	}
	Result<String, CompileError> generate(Node node){
		return this.leftRule.generate(node).and(()->this.rightRule.generate(node)).mapValue(()->tuple.left() + this.locator.unwrap() + tuple.right());
	}
	Result<Node, CompileError> parse(String input){
		var indices=this.locator.locate(input).foldLeft(ArrayList<>.new(), InfixRule::add);
		var errors=ArrayList<CompileError>.new();
		int i=0;
		while(i<indices.size()){
			int index=indices.get(i);
			var left=input.substring(0, index);
			var right=input.substring(index+this.locator.length());
			var result=this.leftRule.parse(left).and(()->this.rightRule.parse(right)).mapValue(()->tuple.left().merge(tuple.right()));
			if(result.isOk()){
				return result;
			}
			else{
				errors.add(result.findError().map(Optional::of).orElseGet(Optional::empty).orElseThrow());
			}
			i++;
		}
		return Err<>.new();
	}
	struct InfixRule new(){
		struct InfixRule this;
		return this;
	}
}