import magma.api.result.Err;import magma.api.result.Result;import magma.app.MapNode;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.NodeContext;struct NodeRule(String propertyKey, Rule childRule) implements Rule{
	struct Table{
		Result<Node, CompileError> parse(String input){
			return this.childRule.parse(input).mapValue(()->new MapNode().withNode(this.propertyKey, node));
		}
		Result<String, CompileError> generate(Node node){
			return node.findNode(this.propertyKey).map(this.childRule::generate).orElseGet(()->new Err<>(new CompileError("Node '"+this.propertyKey + "' was not present", new NodeContext(node))));
		}
	}
	struct Impl{}
}