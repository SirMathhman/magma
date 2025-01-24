import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;struct Rule{
	struct Table{
		Result<Node, CompileError> parse(String input);
		Result<String, CompileError> generate(Node node);
	}
	struct Impl{}
}