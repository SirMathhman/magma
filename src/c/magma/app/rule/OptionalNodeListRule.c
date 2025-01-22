import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import magma.app.rule.divide.DivideRule;import java.util.List;struct OptionalNodeListRule implements Rule{
	String propertyKey;
	Rule ifPresent;
	Rule ifEmpty;
	OrRule rule;
	public OptionalNodeListRule(String propertyKey, Rule ifPresent, Rule ifEmpty){
		this.propertyKey =propertyKey;
		this.ifPresent =ifPresent;
		this.ifEmpty =ifEmpty;
		this.rule = new OrRule(List.of(ifPresent, ifEmpty));
	}
	public OptionalNodeListRule(String propertyKey, DivideRule ifPresent){
		this(propertyKey, ifPresent, ExactRule.new());
	}
	Result<Node, CompileError> parse(String input){
		return this.rule.parse(input);
	}
	Result<String, CompileError> generate(Node node){
		if(node.hasNodeList(this.propertyKey)){
			return this.ifPresent.generate(node);
		}
		else{
			return this.ifEmpty.generate(node);
		}
	}
}