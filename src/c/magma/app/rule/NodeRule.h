import magma.api.result.Err;import magma.api.result.Result;import magma.app.MapNode;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.NodeContext;struct NodeRule(String propertyKey, Rule childRule){
	Result<Node, CompileError> parse(any* _ref_, String input){
		return this.childRule.parse(input).mapValue(()->new MapNode().withNode(this.propertyKey, node));
	}
	Result<String, CompileError> generate(any* _ref_, Node node){
		return node.findNode(this.propertyKey).map(this.childRule::generate).orElseGet(()->new Err<>(new CompileError("Node '"+this.propertyKey + "' was not present", new NodeContext(node))));
	}
	Rule N/A(any* _ref_){
		return N/A.new();
	}
}