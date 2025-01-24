import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.NodeContext;import magma.app.error.context.StringContext;import java.util.List;struct ContextRule(String message, Rule childRule){
	Result<Node, CompileError> parse(String input){
		return this.childRule.parse(input).mapErr(()->new CompileError(this.message, new StringContext(input), List.of(err)));
	}
	Result<String, CompileError> generate(Node node){
		return this.childRule.generate(node).mapErr(()->new CompileError(this.message, new NodeContext(node), List.of(err)));
	}
	Rule N/A(){
		return N/A.new();
	}
}