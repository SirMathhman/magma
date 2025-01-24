import magma.api.result.Ok;import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import magma.app.lang.CommonLang;import java.util.ArrayList;import java.util.List;import static magma.app.pass.Passer.by;struct Formatter implements Passer{
	struct Table{
		Node removeWhitespace(Node block){
			return block.mapNodeList(CommonLang.CONTENT_CHILDREN, ()->{
				return children.stream().filter(()->!child.is("whitespace")).toList();
			});
		}
		Node cleanupNamespaced(Node root){
			return root.mapNodeList(CommonLang.CONTENT_CHILDREN, ()->children.stream().filter(()->!child.is("package")).filter(Formatter::filterImport).toList());
		}
		boolean filterImport(Node child){
			if(!child.is("import"))return true;
			var namespace=child.findString("namespace").orElse("");
			return !namespace.startsWith("java.util.function");
		}
		PassUnit<Node> formatBlock(PassUnit<Node> inner){
			return inner.exit().flattenNode((State state, Node block) -> {
            if (block.findNodeList(CommonLang.CONTENT_CHILDREN).orElse(new ArrayList<Node>()).isEmpty()) {
                return block.removeNodeList(CommonLang.CONTENT_CHILDREN);
            }

            return block.withString(CommonLang.CONTENT_AFTER_CHILDREN, formatIndent(state.depth())).mapNodeList(CommonLang.CONTENT_CHILDREN, children -> attachIndent(state, children));
        });
		}
		List<Node> attachIndent(State state, List<Node> children){
			return children.stream().map(()->child.withString(CommonLang.CONTENT_BEFORE_CHILD, formatIndent(state.depth() + 1))).toList();
		}
		String formatIndent(int state){
			return "\n"+"\t".repeat(state);
		}
		Node cleanupDefinition(Node definition){
			return definition.removeNodeList("annotations").removeNodeList("modifiers");
		}
		Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit){
			return new Ok<>(unit.filter(by("block")).map(Formatter::formatBlock).orElse(unit));
		}
		Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit){
			return new Ok<>(unit.filter(by("block")).map(PassUnit::enter).map(()->inner.mapValue(Formatter::removeWhitespace)).or(()->unit.filterAndMapToValue(by("root"), Formatter::cleanupNamespaced)).or(()->unit.filterAndMapToValue(by("definition"), Formatter::cleanupDefinition)).orElse(unit));
		}
	}
	struct Impl{}
	struct Table table;
	struct Impl impl;
}