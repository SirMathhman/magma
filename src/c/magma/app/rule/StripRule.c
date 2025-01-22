import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;

public StripRule(Rule childRule){
	this(childRule, "", "");
}

Result<Node, CompileError> parse(String input){
	return this.childRule.parse(input.strip());
}

Result<String, CompileError> generate(Node node){
	const auto before=node.findString(this.before).orElse("");
	const auto after=node.findString(this.after).orElse("");
	return this.childRule.generate(node).mapValue(auto _lambda38_(auto content){
		return before+content+after;
	});
}
struct StripRule(
        Rule childRule, String before, String after
) implements Rule {
}

