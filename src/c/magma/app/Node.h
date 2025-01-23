import magma.api.Tuple;import magma.api.stream.Stream;import java.util.List;import java.util.Optional;struct Node{
	struct VTable{
		((void*, String, List<Node>) => Node) withNodeList;
		((void*, String) => Optional<List<Node>>) findNodeList;
		((void*, String, String) => Node) withString;
		((void*, String) => Optional<String>) findString;
		((void*, String, Node) => Node) withNode;
		((void*, int) => String) format;
		((void*, String) => Optional<Node>) findNode;
		((void*, String, [void*, ((void*, String) => String)]) => Node) mapString;
		((void*, Node) => Node) merge;
		((void*) => Stream<Tuple<String, List<Node>>>) streamNodeLists;
		((void*) => Stream<Tuple<String, Node>>) streamNodes;
		((void*) => String) display;
		((void*, String) => Node) retype;
		((void*, String) => boolean) is;
		((void*, String, [void*, ((void*, List<Node>) => List<Node>)]) => Node) mapNodeList;
		((void*, String) => boolean) hasNodeList;
		((void*, String) => Node) removeNodeList;
		((void*, String, [void*, ((void*, Node) => Node)]) => Node) mapNode;
		((void*, String) => boolean) hasNode;
		((void*) => boolean) hasType;
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