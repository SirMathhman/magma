import magma.api.Tuple;import magma.api.stream.Stream;import java.util.List;import java.util.Optional;struct Node{
	struct VTable{
		Node (*)(void*, String, List<Node>) withNodeList;
		Optional<List<Node>> (*)(void*, String) findNodeList;
		Node (*)(void*, String, String) withString;
		Optional<String> (*)(void*, String) findString;
		Node (*)(void*, String, Node) withNode;
		String (*)(void*, int) format;
		Optional<Node> (*)(void*, String) findNode;
		Node (*)(void*, String, [void*, String (*)(void*, String)]) mapString;
		Node (*)(void*, Node) merge;
		Stream<Tuple<String, List<Node>>> (*)(void*) streamNodeLists;
		Stream<Tuple<String, Node>> (*)(void*) streamNodes;
		String (*)(void*) display;
		Node (*)(void*, String) retype;
		boolean (*)(void*, String) is;
		Node (*)(void*, String, [void*, List<Node> (*)(void*, List<Node>)]) mapNodeList;
		boolean (*)(void*, String) hasNodeList;
		Node (*)(void*, String) removeNodeList;
		Node (*)(void*, String, [void*, Node (*)(void*, Node)]) mapNode;
		boolean (*)(void*, String) hasNode;
		boolean (*)(void*) hasType;
	}
	Box<void*> ref;
	struct VTable vtable;
	struct Node new(Box<void*> ref, struct VTable vtable){
		struct Node this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}