import magma.api.result.Err;import magma.api.result.Ok;import magma.api.result.Result;import magma.app.MapNode;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.NodeContext;struct StringRule implements Rule{
String propertyKey;
public StringRule(String propertyKey);
Result<String, CompileError> parse(Node node);
Result<Node, CompileError> parse(String input);
Result<String, CompileError> generate(Node node);
}