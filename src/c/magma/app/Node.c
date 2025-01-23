import magma.api.Tuple;import magma.api.stream.Stream;import java.util.List;import java.util.Optional;struct Node{
	Node withNodeList(String propertyKey, List<Node> propertyValues);
	Optional<List<Node>> findNodeList(String propertyKey);
	Node withString(String propertyKey, String propertyValue);
	Optional<String> findString(String propertyKey);
	Node withNode(String propertyKey, Node propertyValue);
	String format(int depth);
	Optional<Node> findNode(String propertyKey);
	Node mapString(String propertyKey, ((String) => String) mapper);
	Node merge(Node other);
	Stream<Tuple<String, List<Node>>> streamNodeLists();
	Stream<Tuple<String, Node>> streamNodes();
	String display();
	Node retype(String type);
	boolean is(String type);
	Node mapNodeList(String propertyKey, ((List<Node>) => List<Node>) mapper);
	boolean hasNodeList(String propertyKey);
	Node removeNodeList(String propertyKey);
	Node mapNode(String propertyKey, ((Node) => Node) mapper);
	boolean hasNode(String propertyKey);
	boolean hasType();struct Node new(){struct Node this;return this;}
}