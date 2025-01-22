import magma.api.result.Err;import magma.api.result.Result;import magma.app.MapNode;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.NodeContext;struct NodeRule(String propertyKey, Rule childRule) implements Rule{
	Result<Node, CompileError> parse(String input);
	Result<String, CompileError> generate(Node node);
}