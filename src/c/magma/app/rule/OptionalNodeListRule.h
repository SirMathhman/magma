import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
import java.util.List;
final struct OptionalNodeListRule implements Rule {
	const String propertyKey;
	const Rule ifPresent;
	const Rule ifEmpty;
	const OrRule rule;
	public OptionalNodeListRule(String propertyKey, Rule ifPresent, Rule ifEmpty){
		this.propertyKey =propertyKey;
		this.ifPresent =ifPresent;
		this.ifEmpty =ifEmpty;
		this.rule = new OrRule(List.of(ifPresent, ifEmpty));
	}
	@Override
Result<Node, CompileError> parse(String input){
		return this.rule.parse(input);
	}
	@Override
Result<String, CompileError> generate(Node node){
		if(node.hasNodeList(this.propertyKey)){
			return this.ifPresent.generate(node);
		}
		else {
			return this.ifEmpty.generate(node);
		}
	}
}

