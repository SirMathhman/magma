import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import magma.app.rule.divide.DivideRule;import java.util.List;struct OptionalNodeListRule implements Rule{
	String propertyKey;
	Rule ifPresent;
	Rule ifEmpty;
	OrRule rule;
	public OptionalNodeListRule(String propertyKey, Rule ifPresent, Rule ifEmpty);
	public OptionalNodeListRule(String propertyKey, DivideRule ifPresent);
	Result<Node, CompileError> parse(String input);
	Result<String, CompileError> generate(Node node);
}