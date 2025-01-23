import magma.api.result.Err;import magma.api.result.Result;import magma.app.MapNode;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.NodeContext;struct NodeRule(String propertyKey, Rule childRule) implements Rule{
	Result<Node, CompileError> parse(String input){
		return this.childRule.parse(input).mapValue(()->MapNode.new().withNode(this.propertyKey, node));
	}
	Result<String, CompileError> generate(Node node){
		return node.findNode(this.propertyKey).map(this.childRule::generate).orElseGet(()->Err<>.new());
	}struct NodeRule new(){struct NodeRule this;return this;}
}