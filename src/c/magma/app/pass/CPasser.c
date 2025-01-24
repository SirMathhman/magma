import magma.api.result.Ok;import magma.api.result.Result;import magma.app.MapNode;import magma.app.Node;import magma.app.error.CompileError;import java.util.ArrayList;import java.util.List;import static magma.app.lang.CommonLang.CONTENT_CHILDREN;struct CPasser implements Passer{
	struct Table{
		List<Node> getNodes(List<Node> children){
			var methods=new ArrayList<Node>();
			var others=new ArrayList<Node>();
			children.forEach(()->{
				if(child.is("method")){
					methods.add(child);
				}
				else{
					others.add(child);
				}
			});
			var tableDef=new MapNode("struct")
                .withString("name", "Table")
                .withNode("value", new MapNode("block").withNodeList(CONTENT_CHILDREN, methods));
			var implDef=new MapNode("struct")
                .withString("name", "Impl")
                .withNode("value", new MapNode("block").withNodeList(CONTENT_CHILDREN, others));
			return List.of(tableDef, implDef, new MapNode("definition")
                        .withString("name", "table")
                        .withNode("type", new MapNode("struct").withString("value", "Table")), new MapNode("definition")
                        .withString("name", "impl")
                        .withNode("type", new MapNode("struct").withString("value", "Impl")));
		}
		Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit){
			return new Ok<>(unit.filterAndMapToValue(Passer.by("class").or(Passer.by("record")).or(Passer.by("interface")), ()->{
				return node.retype("struct").mapNode("value", ()->{
					return value.mapNodeList("children", CPasser::getNodes);
				});
			}).orElse(unit));
		}
	}
	struct Impl{}
	struct Table table;
	struct Impl impl;
}