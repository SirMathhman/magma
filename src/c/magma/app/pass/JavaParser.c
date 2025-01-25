#include "./JavaParser.h"
struct JavaParser implements Passer{
	Node createDefinition(String parameter){
		return new MapNode("definition").withString("name", parameter);
	}
	Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit){
		var node=unit.value();
		if(node.is("method")){
			return new Ok<>(unit.exit());
		}
		if(node.is("class")||node.is("record")||node.is("interface")){
			return new Ok<>(unit.exit());
		}
		return new Ok<>(unit);
	}
	Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit){
		var node=unit.value();
		if(node.is("lambda")){
			List<Node> definitions;
			var parameterNode=node.findNode(LAMBDA_PARAMETERS);
			var parameterNodeLists=node.findNodeList(LAMBDA_PARAMETERS);
			if(parameterNode.isPresent()){
				var parameter=parameterNode.orElse(new MapNode()).findString("value").orElse("");
				definitions=List.of(createDefinition(parameter));
			}
			else if(parameterNodeLists.isPresent()){
				definitions=parameterNodeLists.orElse(new ArrayList<>()).stream().map(child->child.findString("value")).flatMap(Optional::stream).map(JavaParser::createDefinition).toList();
			}
			else{
				definitions=Collections.emptyList();
			}
			return new Ok<>(unit.enter().define(definitions));
		}
		if(node.is("class")||node.is("record")||node.is("interface")){
			var name=node.findString("name").orElse("");
			var value=node.findNode("value").orElse(new MapNode());
			var children=value.findNodeList("children").orElse(new ArrayList<>());
			var methodDefinitions=children.stream().filter(child->child.is("method")).map(method->method.findNode("definition")).flatMap(Optional::stream).toList();
			return new Ok<>(unit.enter().define(List.of(createDefinition(name))).define(methodDefinitions));
		}
		if(node.is("method")){
			var params=node.findNodeList("params").orElse(Collections.emptyList());
			return new Ok<>(unit.enter().define(params));
		}
		if(node.is("import")){
			var value=node.findNodeList("namespace").orElse(Collections.emptyList()).getLast().findString("value").orElse("");
			return new Ok<>(unit.define(List.of(createDefinition(value))));
		}
		if(node.is("definition")){
			return new Ok<>(unit.define(List.of(node)));
		}
		if(node.is(SYMBOL_VALUE_TYPE)){
			var value=node.findString("value").orElse("");
			if(!value.equals("this")&&!unit.state().find(value).isPresent()&&!isDefaultJavaValue(value)){
				var state=unit.state();
				return new Err<>(new CompileError("Symbol not defined - "+state, new NodeContext(node)));
			}
		}
		return new Ok<>(unit);
	}
}
