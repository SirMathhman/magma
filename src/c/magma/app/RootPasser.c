import magma.api.result.Ok;import magma.api.result.Result;import magma.app.error.CompileError;import java.util.ArrayList;import java.util.List;import static magma.app.lang.CommonLang.CONTENT_CHILDREN;import static magma.app.lang.CommonLang.METHOD_VALUE;struct RootPasser implements Passer{
	Node getStruct(Node node){
		return node.retype("struct").mapNode("value", ()->{
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
	Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit){
		return new Ok<>(unit);
	}
	Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit){
		return new Ok<>(unit.filterAndMapToValue(Passer.by("class").or(Passer.by("record")), ()->node.retype("struct")).or(()->unit.filterAndMapToValue(Passer.by("interface"), RootPasser::getStruct)).orElse(unit));
	}
}