import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.StringContext;
public struct SuffixRule implements Rule {
	private final String suffix;
	private final Rule childRule;
	((Rule, String) => public) SuffixRule=public SuffixRule(Rule childRule, String suffix){
		this.suffix =suffix;
		this.childRule =childRule;
	};
	((String, String) => Result<String, CompileError>) truncateRight=Result<String, CompileError> truncateRight(String input, String slice){
		if(input.endsWith(slice)){
		return new Ok<>(input.substring(0, input.length() - slice.length()));
	}
		else {
		return new Err<>(new CompileError("Suffix '"+slice+"' not present", new StringContext(input)));
	}
	};
	((String) => Result<Node, CompileError>) parse=Result<Node, CompileError> parse(String input){
		return truncateRight(input, this.suffix).flatMapValue(this.childRule::parse);
	};
	((Node) => Result<String, CompileError>) generate=Result<String, CompileError> generate(Node node){
		return childRule.generate(node).mapValue(inner ->inner+suffix);
	};
}