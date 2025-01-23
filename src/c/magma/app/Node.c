import magma.api.Tuple;import magma.api.stream.Stream;import java.util.List;import java.util.Optional;struct Node{
	struct VTable{
		((String, List<Node>) => Node) withNodeList;
		((String) => Optional<List<Node>>) findNodeList;
		((String, String) => Node) withString;
		((String) => Optional<String>) findString;
		((String, Node) => Node) withNode;
		((int) => String) format;
		((String) => Optional<Node>) findNode;
		((String, ((String) => String)) => Node) mapString;
		((Node) => Node) merge;
		(() => Stream<Tuple<String, List<Node>>>) streamNodeLists;
		(() => Stream<Tuple<String, Node>>) streamNodes;
		(() => String) display;
		((String) => Node) retype;
		((String) => boolean) is;
		((String, ((List<Node>) => List<Node>)) => Node) mapNodeList;
		((String) => boolean) hasNodeList;
		((String) => Node) removeNodeList;
		((String, ((Node) => Node)) => Node) mapNode;
		((String) => boolean) hasNode;
		(() => boolean) hasType;
	}
	struct Node new(){
		struct Node this;
		return this;
	}
}