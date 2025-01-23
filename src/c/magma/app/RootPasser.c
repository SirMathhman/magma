import magma.api.result.Ok;import magma.api.result.Result;import magma.app.error.CompileError;import java.util.ArrayList;import java.util.Collections;import java.util.List;import java.util.Optional;import static magma.app.lang.CommonLang.CONTENT_CHILDREN;import static magma.app.lang.CommonLang.FUNCTIONAL_PARAMS;import static magma.app.lang.CommonLang.FUNCTIONAL_RETURN;import static magma.app.lang.CommonLang.FUNCTIONAL_TYPE;import static magma.app.lang.CommonLang.GENERIC_CHILDREN;import static magma.app.lang.CommonLang.GENERIC_PARENT;import static magma.app.lang.CommonLang.GENERIC_TYPE;import static magma.app.lang.CommonLang.METHOD_DEFINITION;import static magma.app.lang.CommonLang.METHOD_PARAMS;import static magma.app.lang.CommonLang.METHOD_TYPE;import static magma.app.lang.CommonLang.METHOD_VALUE;import static magma.app.lang.CommonLang.TUPLE_CHILDREN;import static magma.app.lang.CommonLang.TUPLE_TYPE;struct RootPasser implements Passer{
	Node replaceWithInvocation(Node node){
		var type=node.findString("type").orElse("");
		var symbol=createSymbol(type);
		return node.retype("invocation").withNode("caller", MapNode.new().withString("property", "new"));
	}
	Node replaceWithFunctional(Node generic){
		return tryReplaceWithFunctional(generic).map(()->{
			return MapNode.new().withNodeList(TUPLE_CHILDREN, List.of(createSymbol("Any"), functional));
		}).orElse(generic);
	}
	Optional<Node> tryReplaceWithFunctional(Node generic){
		var parent=generic.findString(GENERIC_PARENT).orElse("");
		var children=generic.findNodeList(GENERIC_CHILDREN).orElse(Collections.emptyList());
		if(parent.equals("Supplier")){
			return Optional.of(MapNode.new());
		}
		if(parent.equals("Function")){
			var params=ArrayList<Node>.new();
			params.add(createAnyType());
			params.add(children.get(0));
			return Optional.of(MapNode.new().withNode("return", children.get(1)));
		}
		return Optional.empty();
	}
	Node retypeToStruct(Node node, List<Node> parameters){
		var name=node.findString("name").orElse("");
		return node.retype("struct").mapNode("value", ()->{
			return value.mapNodeList(CONTENT_CHILDREN, ()->{
				var method=createConstructor(parameters, name);
				Node withParameters;
				if(parameters.isEmpty()){
					withParameters=method;
				}
				else{
					withParameters=method.withNodeList("params", parameters);
				}
				var children1=ArrayList<Node>.new();
				children1.add(withParameters);
				return children1;
			});
		});
	}
	Node createConstructor(List<Node> parameters, String name){
		var thisType=MapNode.new();
		var thisRef=createSymbol("this");
		var thisDef=MapNode.new().withString("name", "this");
		var thisReturn=MapNode.new();
		var constructorChildren=ArrayList<Node>.new();
		constructorChildren.add(thisDef);
		constructorChildren.addAll(parameters.stream().map(RootPasser::createAssignment).toList());
		constructorChildren.add(thisReturn);
		var propertyValue=MapNode.new().withNodeList(CONTENT_CHILDREN, constructorChildren);
		var propertyValue1=MapNode.new();
		return MapNode.new().withNode(METHOD_DEFINITION, propertyValue1).withNode(METHOD_VALUE, propertyValue);
	}
	Node createAssignment(Node parameter){
		var paramName=parameter.findString("name").orElse("");
		var propertyValue=MapNode.new().withString("property", paramName);
		return MapNode.new().withNode("source", createSymbol(paramName));
	}
	Node createSymbol(String value){
		return MapNode.new();
	}
	Predicate<Node> by(String type){
		return ()->node.is(type);
	}
	Node replaceWithDefinition(Node node){
		var value=node.findNode(METHOD_VALUE);
		if(value.isEmpty()){
			var params=ArrayList<Node>.new();
			params.add(createAnyType());
			params.addAll(node.findNodeList(METHOD_PARAMS).orElse(ArrayList<>.new()).stream().map(()->param.findNode("type")).flatMap(Optional::stream).toList());
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
	Node passInterface(Node node){
		var tableType=MapNode.new();
		var tableDefinition=MapNode.new().withString("name", "vtable");
		var refType=MapNode.new().withString(GENERIC_PARENT, "Box").withNodeList(GENERIC_CHILDREN, List.of(createAnyType()));
		var refDefinition=MapNode.new();
		var node1=node.mapNode("value", ()->{
			return value.mapNodeList(CONTENT_CHILDREN, ()->{
				var table=MapNode.new();
				return List.of(table, refDefinition, tableDefinition);
			});
		});
		return retypeToStruct(node1, List.of(refDefinition, tableDefinition));
	}
	Node createAnyType(){
		return createSymbol("void*");
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