import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;struct StripRule(
        Rule childRule, String before, String after
) implements Rule{
	public StripRule(Rule childRule){
		this(childRule, "", "");
	}
	Result<Node, CompileError> parse(String input){
		return this.childRule.parse(input.strip());
	}
	Result<String, CompileError> generate(Node node){
		var before=node.findString(this.before).orElse("");
		var after=node.findString(this.after).orElse("");
		return this.childRule.generate(node).mapValue(()->before+content+after);
	}
}