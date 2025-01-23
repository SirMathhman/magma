import magma.api.Tuple;import magma.api.stream.Stream;import magma.api.stream.Streams;import java.util.HashMap;import java.util.List;import java.util.Map;import java.util.Optional;import java.util.StringJoiner;import java.util.stream.Collectors;struct MapNode implements Node{
	Map<String, String> strings;
	Map<String, List<Node>> nodeLists;
	Map<String, Node> nodes;
	Optional<String> type;
	public MapNode(){
		this(Optional.empty(), HashMap<>.new(), HashMap<>.new(), HashMap<>.new());
	}
	public MapNode(Optional<String> type, Map<String, String> strings, Map<String, Node> nodes, Map<String, List<Node>> nodeLists){
		this.type =type;
		this.strings =strings;
		this.nodes =nodes;
		this.nodeLists =nodeLists;
	}
	public MapNode(String type){
		this(Optional.of(type), HashMap<>.new(), HashMap<>.new(), HashMap<>.new());
	}
	StringBuilder createEntry(String name, String content, int depth){
		return StringBuilder.new().append("\n"+"\t".repeat(depth)).append(name).append(" : ").append(content);
	}
	String toString(){
		return format(0);
	}
	String format(int depth){
		var typeString=this.type.map(()->inner+" ").orElse("");
		var builder=StringBuilder.new().append(typeString).append("{");
		var joiner=StringJoiner.new();
		this.strings.entrySet().stream().map(()->createEntry(entry.getKey(), "\""+entry.getValue() + "\"", depth+1)).forEach(joiner::add);
		this.nodes.entrySet().stream().map(()->createEntry(entry.getKey(), entry.getValue().format(depth+1), depth+1)).forEach(joiner::add);
		this.nodeLists.entrySet().stream().map(()->createEntry(entry.getKey(), entry.getValue().stream().map(()->node.format(depth+1)).collect(Collectors.joining(",\n", "[", "]")), depth+1)).forEach(joiner::add);
		builder.append(joiner);
		return builder.append("\n").append("\t".repeat(depth)).append("}").toString();
	}
	Optional<Node> findNode(String propertyKey){
		return Optional.ofNullable(this.nodes.get(propertyKey));
	}
	Node mapString(String propertyKey, ((String) => String) mapper){
		return findString(propertyKey).map(mapper).map(()->withString(propertyKey, newString)).orElse(this);
	}
	Node merge(Node other){
		var withStrings=stream(this.strings).foldLeft(other, (node, tuple) -> node.withString(tuple.left(), tuple.right()));
		var withNodes=streamNodes().foldLeft(withStrings, (node, tuple) -> node.withNode(tuple.left(), tuple.right()));
		return streamNodeLists().foldLeft(withNodes, (node, tuple) -> node.withNodeList(tuple.left(), tuple.right()));
	}
	Stream<Tuple<String, List<Node>>> streamNodeLists(){
		return stream(this.nodeLists);
	}
	Stream<Tuple<String, Node>> streamNodes(){
		return stream(this.nodes);
	}
	<K, V>Stream<Tuple<K, V>> stream(Map<K, V> map){
		return Streams.from(map.entrySet()).map(()->Tuple<>.new());
	}
	String display(){
		return toString();
	}
	Node retype(String type){
		return MapNode.new();
	}
	boolean is(String type){
		return this.type.isPresent() && this.type.get().equals(type);
	}
	Node mapNodeList(String propertyKey, ((List<Node>) => List<Node>) mapper){
		return findNodeList(propertyKey).map(mapper).map(()->withNodeList(propertyKey, list)).orElse(this);
	}
	boolean hasNodeList(String propertyKey){
		return this.nodeLists.containsKey(propertyKey);
	}
	Node removeNodeList(String propertyKey){
		var copy=HashMap<>.new();
		copy.remove(propertyKey);
		return MapNode.new();
	}
	Node mapNode(String propertyKey, ((Node) => Node) mapper){
		return findNode(propertyKey).map(mapper).map(()->withNode(propertyKey, node)).orElse(this);
	}
	boolean hasNode(String propertyKey){
		return this.nodes.containsKey(propertyKey);
	}
	boolean hasType(){
		return this.type.isPresent();
	}
	Node withNode(String propertyKey, Node propertyValue){
		var copy=HashMap<>.new();
		copy.put(propertyKey, propertyValue);
		return MapNode.new();
	}
	Node withNodeList(String propertyKey, List<Node> propertyValues){
		var copy=HashMap<>.new();
		copy.put(propertyKey, propertyValues);
		return MapNode.new();
	}
	Optional<List<Node>> findNodeList(String propertyKey){
		return Optional.ofNullable(this.nodeLists.get(propertyKey));
	}
	Node withString(String propertyKey, String propertyValues){
		var copy=HashMap<>.new();
		copy.put(propertyKey, propertyValues);
		return MapNode.new();
	}
	Optional<String> findString(String propertyKey){
		return Optional.ofNullable(this.strings.get(propertyKey));
	}struct MapNode implements Node new(){struct MapNode implements Node this;return this;}
}