import magma.api.result.Ok;import magma.api.result.Result;import magma.app.error.CompileError;import java.util.ArrayList;import java.util.List;import static magma.app.lang.CommonLang.CONTENT_CHILDREN;import static magma.app.lang.CommonLang.GENERIC_PARENT;import static magma.app.lang.CommonLang.METHOD_VALUE;struct RootPasser implements Passer{
	Node getStruct(Node node){
		return convertToStruct(node).mapNode("value", ()->{
			return value.mapNodeList(CONTENT_CHILDREN, ()->{
				var copy=new ArrayList<Node>(children);
				var thisType=new MapNode("struct").withString("value", "Impl");
				var thisTypeDefinition=new MapNode("definition")
                        .withNode("type", thisType);
				var constructorDefinition=thisTypeDefinition.withString("name", "new");
				var thisSymbol=new MapNode("symbol").withString("value", "this");
				var constructorBody=new MapNode("block")
                        .withNodeList("children", List.of(thisTypeDefinition.withString("name", "this"), new MapNode("return").withNode("value", thisSymbol)));
				var constructor=new MapNode("method")
                        .withNode("definition", constructorDefinition).withNode(METHOD_VALUE, constructorBody);
				var block=new MapNode("block").withNodeList(CONTENT_CHILDREN, List.of(constructor));
				var impl=new MapNode("struct")
                        .withString("name", "Impl")
                        .withNode("value", block);
				copy.add(impl);
				return copy;
			});
		});
	}
	Node convertToStruct(Node node){
		return node.retype("struct").mapNode("value", ()->value.mapNodeList("children", ()->{
			var maybeSupertype=node.findNode("supertype");
			if(maybeSupertype.isPresent()){
				var supertype=maybeSupertype.get();
				return attachConverter(children, supertype);
			}
			return children;
		}).removeNode("supertype"));
	}
	List<Node> attachConverter(List<Node> children, Node supertype){
		var name=supertype.findString("name").or(()->supertype.findString(GENERIC_PARENT)).orElse("N/A");
		var copy=new ArrayList<Node>(children);
		var converterDefinition=new MapNode("definition")
                .withNode("type", supertype).withString("name", name);
		var supertypeRef=new MapNode("symbol")
                .withString("value", name);
		var caller=new MapNode("data-access")
                .withNode("ref", supertypeRef).withString("property", "new");
		var returnsValue=new MapNode("invocation").withNode("caller", caller);
		var returns=new MapNode("return").withNode("value", returnsValue);
		var converterBody=new MapNode("block").withNodeList("children", List.of(returns));
		var converter=new MapNode("method")
                .withNode("definition", converterDefinition).withNode("value", converterBody);
		copy.add(converter);
		return copy;
	}
	Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit){
		return new Ok<>(unit);
	}
	Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit){
		return new Ok<>(unit.filterAndMapToValue(Passer.by("class").or(Passer.by("record")), ()->convertToStruct(node)).or(()->unit.filterAndMapToValue(Passer.by("interface"), RootPasser::getStruct)).orElse(unit));
	}
	Passer N/A(){
		return N/A.new();
	}
}