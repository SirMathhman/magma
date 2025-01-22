import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.NodeContext;import magma.app.error.context.StringContext;import java.util.List;struct ContextRule(String message, Rule childRule) implements Rule{
	Result<Node, CompileError> parse(String input){
		return this.childRule.parse(input).mapErr(()->CompileError.new());
	}
	Result<String, CompileError> generate(Node node){
		return this.childRule.generate(node).mapErr(()->CompileError.new());
	}
}