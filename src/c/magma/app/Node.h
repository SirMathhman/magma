import magma.api.Tuple;import magma.api.stream.Stream;import java.util.List;import java.util.Optional;struct Node{
	struct VTable{
		((Any, String, List<Node>) => Node) withNodeList;
		((Any, String) => Optional<List<Node>>) findNodeList;
		((Any, String, String) => Node) withString;
		((Any, String) => Optional<String>) findString;
		((Any, String, Node) => Node) withNode;
		((Any, int) => String) format;
		((Any, String) => Optional<Node>) findNode;
		((Any, String, [Any, ((Any, String) => String)]) => Node) mapString;
		((Any, Node) => Node) merge;
		((Any) => Stream<Tuple<String, List<Node>>>) streamNodeLists;
		((Any) => Stream<Tuple<String, Node>>) streamNodes;
		((Any) => String) display;
		((Any, String) => Node) retype;
		((Any, String) => boolean) is;
		((Any, String, [Any, ((Any, List<Node>) => List<Node>)]) => Node) mapNodeList;
		((Any, String) => boolean) hasNodeList;
		((Any, String) => Node) removeNodeList;
		((Any, String, [Any, ((Any, Node) => Node)]) => Node) mapNode;
		((Any, String) => boolean) hasNode;
		((Any) => boolean) hasType;
	}
	struct VTable vtable;
	struct Node new(struct VTable table){
		struct Node this;
		this.table=table;
		return this;
	}
}