import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
public struct StripRule(
        Rule childRule, String before, String after
) implements Rule {
	((Rule) => public) StripRule=public StripRule(Rule childRule){
		this(childRule, "", "");
	};
	((String) => Result<Node, CompileError>) parse=Result<Node, CompileError> parse(String input){
		return this.childRule.parse(input.strip());
	};
	((Node) => Result<String, CompileError>) generate=Result<String, CompileError> generate(Node node){
		final var before=node.findString(this.before).orElse("");
		final var after=node.findString(this.after).orElse("");
		return this.childRule.generate(node).mapValue(content ->before+content+after);
	};
}