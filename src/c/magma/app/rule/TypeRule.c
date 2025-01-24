import magma.api.result.Err;import magma.api.result.Result;import magma.app.MapNode;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.NodeContext;import magma.app.error.context.StringContext;import java.util.List;struct TypeRule(String type, Rule rule){
	Result<Node, CompileError> parse(any* _ref_, String input){
		return this.rule.parse(input).mapValue(()->node.retype(this.type)).mapValue(()->{
			if(type.equals("method")){
				System.out.println("\t"+node.findNode("definition").orElse(new MapNode()).findString("name").orElse(""));
			}
			return node;
		}).mapErr(()->new CompileError("Failed to parse type '"+this.type + "'", new StringContext(input), List.of(err)));
	}
	Result<String, CompileError> generate(any* _ref_, Node node){
		if(node.is(this.type)){
			return this.rule.generate(node).mapErr(()->new CompileError("Failed to generate type '"+this.type + "'", new NodeContext(node), List.of(err)));
		}
		else{
			return new Err<>(new CompileError("Node was not of type '"+this.type + "'", new NodeContext(node)));
		}
	}
	Rule N/A(){
		return N/A.new();
	}
}