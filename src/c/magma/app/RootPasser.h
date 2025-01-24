import magma.api.result.Ok;import magma.api.result.Result;import magma.app.error.CompileError;import java.util.ArrayList;import java.util.List;import static magma.app.lang.CommonLang.CONTENT_CHILDREN;import static magma.app.lang.CommonLang.GENERIC_CHILDREN;import static magma.app.lang.CommonLang.GENERIC_CONSTRUCTOR;import static magma.app.lang.CommonLang.METHOD_VALUE;struct RootPasser{
	Node unwrapInterface(Node node){
		return convertToStruct(node).mapNode("value", ()->value.mapNodeList(CONTENT_CHILDREN, ()->{
			var copy=new ArrayList<>(children);
			var impl=createConstructor();
			copy.add(impl);
			return copy;
		}));
	}
	Node createConstructor(){
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
		return new MapNode("struct")
                .withString("name", "Impl")
                .withNode("value", block);
	}
	Node convertToStruct(Node node){
		return node.retype("struct").mapNode("value", ()->value.mapNodeList("children", ()->{
			var maybeSupertype=node.findNode("supertype");
			if(maybeSupertype.isPresent()){
				var supertype=maybeSupertype.get();
				var converter=createConverter(supertype);
				var copy=new ArrayList<>(children);
				copy.add(converter);
				return copy;
			}
			return children;
		})).removeNode("supertype");
	}
	Node createConverter(Node supertype){
		var name=supertype.findString("name").or(()->supertype.findString(GENERIC_CONSTRUCTOR)).orElse("N/A");
		var converterDefinition=new MapNode("definition")
                .withNode("type", supertype).withString("name", name);
		var supertypeRef=new MapNode("symbol")
                .withString("value", name);
		var caller=new MapNode("data-access")
                .withNode("ref", supertypeRef).withString("property", "new");
		var returnsValue=new MapNode("invocation").withNode("caller", caller);
		var returns=new MapNode("return").withNode("value", returnsValue);
		var converterBody=new MapNode("block").withNodeList("children", List.of(returns));
		return new MapNode("method")
                .withNode("definition", converterDefinition).withNode("value", converterBody);
	}
	Node mapToFunctional(Node node){
		var optional=node.findString(GENERIC_CONSTRUCTOR);
		if(optional.isPresent()){
			var constructor=optional.get();
			var children=node.findNodeList(GENERIC_CHILDREN).orElse(new ArrayList<>());
			if(constructor.equals("Supplier")){
				var returns=children.get(0);
				return new MapNode("functional").withNode("return", returns);
			}
			if(constructor.equals("Function")){
				var param=children.get(0);
				var returns=children.get(1);
				return new MapNode("functional")
                        .withNodeList("params", List.of(param))
                        .withNode("return", returns);
			}
		}
		return node;
	}
	Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit){
		return new Ok<>(unit);
	}
	Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit){
		return new Ok<>(unit.filterAndMapToValue(Passer.by("class").or(Passer.by("record")), RootPasser::convertToStruct).or(()->unit.filterAndMapToValue(Passer.by("interface"), RootPasser::unwrapInterface)).or(()->unit.filterAndMapToValue(Passer.by("generic"), RootPasser::mapToFunctional)).orElse(unit));
	}
	Passer N/A(){
		return N/A.new();
	}
}