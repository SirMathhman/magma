import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import java.util.List;struct OptionalNodeRule implements Rule{
	String propertyKey;
	Rule ifPresent;
	Rule ifEmpty;
	OrRule rule;
	public OptionalNodeRule(String propertyKey, Rule ifPresent, Rule ifEmpty);
	public OptionalNodeRule(String modifiers, Rule ifPresent);
	Result<Node, CompileError> parse(String input);
	Result<String, CompileError> generate(Node node);
}