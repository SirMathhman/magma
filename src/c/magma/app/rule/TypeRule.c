import magma.api.result.Err;import magma.api.result.Result;import magma.app.MapNode;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.NodeContext;import magma.app.error.context.StringContext;import java.util.List;struct TypeRule(String type, Rule rule) implements Rule{
	Result<Node, CompileError> parse(String input){
		return this.rule.parse(input).mapValue(()->node.retype(this.type)).mapValue(()->{
			if(type.equals("method")){
				System.out.println("\t"+node.findNode("definition").orElse(MapNode.new()).findString("name").orElse(""));
			}
			return node;
		}).mapErr(()->CompileError.new());
	}
	Result<String, CompileError> generate(Node node){
		if(node.is(this.type)){
			return this.rule.generate(node).mapErr(()->CompileError.new());
		}
		else{
			return Err<>.new();
		}
	}struct TypeRule(String type, Rule rule) implements Rule new(){struct TypeRule(String type, Rule rule) implements Rule this;return this;}
}