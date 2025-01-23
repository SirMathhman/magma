import magma.api.result.Ok;import magma.api.result.Result;import magma.app.error.CompileError;import magma.app.lang.CommonLang;import java.util.ArrayList;import java.util.Collections;import java.util.List;struct RootPasser implements Passer{
	Node replaceWithInvocation(Node node){
		var type=node.findString("type").orElse("");
		var symbol=MapNode.new();
		return node.retype("invocation").withNode("caller", MapNode.new().withString("property", "new"));
	}
	Node replaceWithFunctional(Node generic){
		var parent=generic.findString(CommonLang.GENERIC_PARENT).orElse("");
		var children=generic.findNodeList(CommonLang.GENERIC_CHILDREN).orElse(Collections.emptyList());
		if(parent.equals("Supplier")){
			return MapNode.new();
		}
		if(parent.equals("Function")){
			return MapNode.new();
		}
		return generic;
	}
	Node retypeToStruct(Node node){
		var name=node.findString("name").orElse("");
		return node.retype("struct").mapNode("value", ()->{
			return value.mapNodeList(CommonLang.CONTENT_CHILDREN, ()->{
				var thisType=MapNode.new();
				var children1=ArrayList<Node>.new();
				var propertyValue=MapNode.new().withNodeList(CommonLang.CONTENT_CHILDREN, List.of(MapNode.new().withString("name", "this"), MapNode.new()));
				children1.add(MapNode.new());
				return children1;
			});
		});
	}
	Predicate<Node> by(String type){
		return ()->node.is(type);
	}
	Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit){
		return Ok<>.new();
	}
	Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit){
		return Ok<>.new();
	}
	struct RootPasser new(){
		struct RootPasser this;
		return this;
	}
}