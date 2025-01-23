import magma.api.result.Ok;import magma.api.result.Result;import magma.app.error.CompileError;import magma.app.lang.CommonLang;import java.util.ArrayList;import java.util.Collections;import java.util.List;import java.util.Optional;import static magma.app.lang.CommonLang.CONTENT_CHILDREN;import static magma.app.lang.CommonLang.FUNCTIONAL_PARAMS;import static magma.app.lang.CommonLang.FUNCTIONAL_RETURN;import static magma.app.lang.CommonLang.FUNCTIONAL_TYPE;import static magma.app.lang.CommonLang.METHOD_DEFINITION;import static magma.app.lang.CommonLang.METHOD_PARAMS;import static magma.app.lang.CommonLang.METHOD_TYPE;import static magma.app.lang.CommonLang.METHOD_VALUE;struct RootPasser implements Passer{
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
	Node replaceWithDefinition(Node node){
		var value=node.findNode(METHOD_VALUE);
		if(value.isEmpty()){
			var params=node.findNodeList(METHOD_PARAMS).orElse(ArrayList<>.new()).stream().map(()->param.findNode("type")).flatMap(Optional::stream).toList();
			return node.findNode(METHOD_DEFINITION).orElse(MapNode.new()).mapNode("type", ()->{
				var withType=MapNode.new().withNode(FUNCTIONAL_RETURN, type);
				if(params.isEmpty()){
					return withType;
				}
				else{
					return withType.withNodeList(FUNCTIONAL_PARAMS, params);
				}
			});
		}
		return node;
	}
	Node getNode(Node node){
		var node1=node.mapNode("value", ()->{
			return value.mapNodeList(CONTENT_CHILDREN, ()->{
				return List.of(MapNode.new());
			});
		});
		return retypeToStruct(node1);
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