import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;struct StripRule(
        Rule childRule, String before, String after
) implements Rule{
	public StripRule(Rule childRule);
	Result<Node, CompileError> parse(String input);
	Result<String, CompileError> generate(Node node);}