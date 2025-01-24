import magma.api.Tuple;import magma.api.stream.Stream;import java.util.List;import java.util.Optional;struct Node{
	Node withNodeList(any* _ref_, String propertyKey, List<Node> propertyValues);
	Optional<List<Node>> findNodeList(any* _ref_, String propertyKey);
	Node withString(any* _ref_, String propertyKey, String propertyValue);
	Optional<String> findString(any* _ref_, String propertyKey);
	Node withNode(any* _ref_, String propertyKey, Node propertyValue);
	String format(any* _ref_, int depth);
	Optional<Node> findNode(any* _ref_, String propertyKey);
	Node mapString(any* _ref_, String propertyKey, Tuple<any*, String (*)(any*, String)> mapper);
	Node merge(any* _ref_, Node other);
	Stream<Tuple<String, List<Node>>> streamNodeLists(any* _ref_);
	Stream<Tuple<String, Node>> streamNodes(any* _ref_);
	String display(any* _ref_);
	Node retype(any* _ref_, String type);
	boolean is(any* _ref_, String type);
	Node mapNodeList(any* _ref_, String propertyKey, Tuple<any*, List<Node> (*)(any*, List<Node>)> mapper);
	boolean hasNodeList(any* _ref_, String propertyKey);
	Node removeNodeList(any* _ref_, String propertyKey);
	Node mapNode(any* _ref_, String propertyKey, Tuple<any*, Node (*)(any*, Node)> mapper);
	boolean hasNode(any* _ref_, String propertyKey);
	boolean hasType(any* _ref_);
	Node removeNode(any* _ref_, String propertyKey);
	struct Impl{
		struct Impl new(any* _ref_){
			struct Impl this;
			return this;
		}
	}
}