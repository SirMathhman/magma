import magma.api.result.Ok;import magma.api.result.Result;import magma.api.stream.Streams;import magma.app.error.CompileError;import java.util.ArrayList;import java.util.Collections;import java.util.List;import static magma.app.lang.CommonLang.CONTENT_AFTER_CHILDREN;import static magma.app.lang.CommonLang.CONTENT_BEFORE_CHILD;import static magma.app.lang.CommonLang.CONTENT_CHILDREN;import static magma.app.lang.CommonLang.GENERIC_CHILDREN;import static magma.app.lang.CommonLang.GENERIC_PARENT;import static magma.app.lang.CommonLang.GENERIC_TYPE;struct PassingStage{
	Result<PassUnit<Node>, CompileError> pass(PassUnit<Node> unit){
		return beforePass(unit).flatMapValue(PassingStage::passNodes).flatMapValue(PassingStage::passNodeLists).flatMapValue(PassingStage::afterPass);
	}
	Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit){
		return Ok<>.new();
	}
	Node replaceWithInvocation(Node node){
		var type=node.findString("type").orElse("");
		var symbol=MapNode.new();
		return node.retype("invocation").withNode("caller", MapNode.new().withString("property", "new"));
	}
	Node replaceWithFunctional(Node generic){
		var parent=generic.findString(GENERIC_PARENT).orElse("");
		var children=generic.findNodeList(GENERIC_CHILDREN).orElse(Collections.emptyList());
		if(parent.equals("Supplier")){
			return MapNode.new();
		}
		if(parent.equals("Function")){
			return MapNode.new();
		}
		return generic;
	}
	Node removeWhitespace(Node block){
		return block.mapNodeList(CONTENT_CHILDREN, ()->{
			return children.stream().filter(()->!child.is("whitespace")).toList();
		});
	}
	Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit){
		return Ok<>.new();
	}
	PassUnit<Node> formatBlock(PassUnit<Node> inner){
		return inner.exit().flattenNode((State state, Node block) -> {
            if (block.findNodeList(CONTENT_CHILDREN).orElse(ArrayList<>.new()).isEmpty()) {
                return block.removeNodeList(CONTENT_CHILDREN);
            }

            return block
                    .withString(CONTENT_AFTER_CHILDREN, formatIndent(state.depth())).mapNodeList(CONTENT_CHILDREN, children -> children.stream().map(()->child.withString(CONTENT_BEFORE_CHILD, formatIndent(state.depth() + 1))).toList());
        });
	}
	String formatIndent(int state){
		return "\n"+"\t".repeat(state);
	}
	Node pruneDefinition(Node definition){
		return definition.removeNodeList("annotations").removeNodeList("modifiers");
	}
	Node retypeToStruct(Node node){
		return node.retype("struct");
	}
	Node removePackageStatements(Node root){
		return root.mapNodeList(CONTENT_CHILDREN, ()->children.stream().filter(()->!child.is("package")).filter(PassingStage::filterImport).toList());
	}
	boolean filterImport(Node child){
		if(!child.is("import"))return true;
		var namespace=child.findString("namespace").orElse("");
		return !namespace.startsWith("java.util.function");
	}
	Predicate<Node> by(String type){
		return ()->node.is(type);
	}
	Result<PassUnit<Node>, CompileError> passNodeLists(PassUnit<Node> unit){
		return unit.value().streamNodeLists().foldLeftToResult(unit, (current, tuple) -> {
            final var propertyKey = tuple.left();
            final var propertyValues = tuple.right();
            return Streams.from(propertyValues).foldLeftToResult(current.withValue(ArrayList<>.new()), PassingStage::passAndAdd).mapValue(unit1 -> unit1.mapValue(node -> current.value().withNodeList(propertyKey, node)));
        });
	}
	Result<PassUnit<List<Node>>, CompileError> passAndAdd(PassUnit<List<Node>> unit, Node element){
		return pass(unit.withValue(element)).mapValue(()->result.mapValue(()->add(unit, value)));
	}
	List<Node> add(PassUnit<List<Node>> unit2, Node value){
		var copy=ArrayList<>.new();
		copy.add(value);
		return copy;
	}
	Result<PassUnit<Node>, CompileError> passNodes(PassUnit<Node> unit){
		return unit.value().streamNodes().foldLeftToResult(unit, (current, tuple) -> {
            final var pairKey = tuple.left();
            final var pairNode = tuple.right();

            return pass(current.withValue(pairNode)).mapValue(passed -> passed.mapValue(value -> current.value().withNode(pairKey, value)));
        });
	}
}