import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.StringContext;
struct SuffixRule implements Rule {
	final String suffix;
	final Rule childRule;
	public SuffixRule(Rule childRule, String suffix){
		this.suffix =suffix;
		this.childRule =childRule;
	}
	static Result<String, CompileError> truncateRight(String input, String slice){
		if(input.endsWith(slice)){
			return new Ok<>(input.substring(0, input.length() - slice.length()));
		}
		else {
			return new Err<>(new CompileError("Suffix '"+slice+"' not present", new StringContext(input)));
		}
	}
	@Override
Result<Node, CompileError> parse(String input){
		return truncateRight(input, this.suffix).flatMapValue(this.childRule::parse);
	}
	@Override
Result<String, CompileError> generate(Node node){
		return childRule.generate(node).mapValue(inner ->inner+suffix);
	}
}
