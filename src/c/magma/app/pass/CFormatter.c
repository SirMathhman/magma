struct CFormatter implements Passer{
	Node removeWhitespace(Node block){
		return block.mapNodeList(CommonLang.CONTENT_CHILDREN, ()->{
			return children.stream().filter(()->!child.is("whitespace")).toList();
		});
	}
	Node cleanupNamespaced(Node root){
		var oldChildren=root.findNodeList(CommonLang.CONTENT_CHILDREN).orElse(Collections.emptyList());
		var newChildren=oldChildren.stream().filter(()->!child.is("package")).filter(CFormatter::filterImport).map(()->child.withString(CONTENT_AFTER_CHILD, "\n")).toList();
		var headerElements=new ArrayList<Node>();
		var sourceElements=new ArrayList<Node>();
		newChildren.forEach(()->{
			if(child.is("include")){
				headerElements.add(child);
			}
			else{
				sourceElements.add(child);
			}
		});
		return new MapNode().withNode("header", createRoot(headerElements)).withNode("source", createRoot(sourceElements));
	}
	Node createRoot(List<Node> elements){
		var node=new MapNode("root");
		return elements.isEmpty()
                ? node
                : node.withNodeList(CONTENT_CHILDREN, elements);
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
		return new Ok<>(unit.filter(by("block")).map(CFormatter::formatBlock).or(()->unit.filterAndMapToValue(by("root"), CFormatter::cleanupNamespaced)).orElse(unit));
	}
	Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit){
		return new Ok<>(unit.filter(by("block")).map(PassUnit::enter).map(()->inner.mapValue(CFormatter::removeWhitespace)).or(()->unit.filterAndMapToValue(by("definition"), CFormatter::cleanupDefinition)).orElse(unit));
	}
}
