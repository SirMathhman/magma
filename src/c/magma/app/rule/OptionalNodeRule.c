import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import java.util.List;struct OptionalNodeRule{
	String propertyKey;
	Rule ifPresent;
	Rule ifEmpty;
	OrRule rule;
	public OptionalNodeRule(any* _ref_, String propertyKey, Rule ifPresent, Rule ifEmpty){
		this.propertyKey =propertyKey;
		this.ifPresent =ifPresent;
		this.ifEmpty =ifEmpty;
		this.rule = new OrRule(List.of(ifPresent, ifEmpty));
	}
	public OptionalNodeRule(any* _ref_, String modifiers, Rule ifPresent){
		this(modifiers, ifPresent, new ExactRule(""));
	}
	Result<Node, CompileError> parse(any* _ref_, String input){
		return this.rule.parse(input);
	}
	Result<String, CompileError> generate(any* _ref_, Node node){
		if(node.hasNode(this.propertyKey)){
			return this.ifPresent.generate(node);
		}
		else{
			return this.ifEmpty.generate(node);
		}
	}
	Rule N/A(){
		return N/A.new();
	}
}