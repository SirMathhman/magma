import magma.api.result.Err;import magma.api.result.Ok;import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.StringContext;struct SuffixRule implements Rule{
	String suffix;
	Rule childRule;
	public SuffixRule(Rule childRule, String suffix);
	Result<String, CompileError> truncateRight(String input, String slice);
	Result<Node, CompileError> parse(String input);
	Result<String, CompileError> generate(Node node);
}