#include "./CFormatter.h"
struct CFormatter implements Passer{
	Node removeWhitespace(Node block){
		return block.mapNodeList(CommonLang.CONTENT_CHILDREN, ()->{
			return children.stream().filter(()->!child.is("whitespace")).toList();
		});
	}
	Node createSegment(String value){
		return new MapNode("segment").withString("value", value);
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
	PassUnit<Node> cleanupNamespaced(PassUnit<Node> unit){
		var namespace=unit.findNamespace();
		var name=unit.findName();
		var oldChildren=unit.value().findNodeList(CommonLang.CONTENT_CHILDREN).orElse(Collections.emptyList());
		var newChildren=oldChildren.stream().filter(()->!child.is("package")).filter(CFormatter::filterImport).map(()->child.withString(CONTENT_AFTER_CHILD, "\n")).toList();
		var joined=String.join("_", namespace) + "_" + name;
		var headerElements=new ArrayList<>(List.of(new MapNode("if-not-defined").withString(CONTENT_AFTER_CHILD, "\n").withString("value", joined), new MapNode("define").withString(CONTENT_AFTER_CHILD, "\n").withString("value", joined)));
		var sourceImport=new MapNode("include").withString(CONTENT_AFTER_CHILD, "\n").withNodeList("namespace", List.of(createSegment("."), createSegment(name)));
		var sourceElements=new ArrayList<>(List.of(sourceImport));
		newChildren.forEach(()->{
			if(child.is("include")){
				headerElements.add(child);
			}
			else{
				sourceElements.add(child);
			}
		});
		headerElements.add(new MapNode("endif").withString(CONTENT_AFTER_CHILD, "\n"));
		return unit.withValue(new MapNode().withNode("header", createRoot(headerElements)).withNode("source", createRoot(sourceElements)));
	}
	Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit){
		return new Ok<>(unit.filter(by("block")).map(CFormatter::formatBlock).or(()->unit.filter(by("root")).map(CFormatter::cleanupNamespaced)).orElse(unit));
	}
	Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit){
		return new Ok<>(unit.filter(by("block")).map(PassUnit::enter).map(()->inner.mapValue(CFormatter::removeWhitespace)).or(()->unit.filterAndMapToValue(by("definition"), CFormatter::cleanupDefinition)).orElse(unit));
	}
}
