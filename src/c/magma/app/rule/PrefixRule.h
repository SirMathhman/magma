import magma.api.result.Err;import magma.api.result.Ok;import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.StringContext;struct PrefixRule implements Rule{
	String prefix;
	Rule childRule;
	public PrefixRule(String prefix, Rule childRule);
	Result<String, CompileError> truncateLeft(String input, String slice);
	Result<Node, CompileError> parse(String input);
	Result<String, CompileError> generate(Node node);}