import magma.api.Tuple;import magma.api.stream.Stream;import magma.api.stream.Streams;import java.util.HashMap;import java.util.List;import java.util.Map;import java.util.Optional;import java.util.StringJoiner;import java.util.stream.Collectors;struct MapNode{
	Map<String, String> strings;
	Map<String, List<Node>> nodeLists;
	Map<String, Node> nodes;
	Optional<String> type;
	public MapNode(){
		this(Optional.empty(), new HashMap<>(), new HashMap<>(), new HashMap<>());
	}
	public MapNode(any* _ref_, Optional<String> type, Map<String, String> strings, Map<String, Node> nodes, Map<String, List<Node>> nodeLists){
		this.type =type;
		this.strings =strings;
		this.nodes =nodes;
		this.nodeLists =nodeLists;
	}
	public MapNode(any* _ref_, String type){
		this(Optional.of(type), new HashMap<>(), new HashMap<>(), new HashMap<>());
	}
	StringBuilder createEntry(any* _ref_, String name, String content, int depth){
		return new StringBuilder().append("\n"+"\t".repeat(depth)).append(name).append(" : ").append(content);
	}
	String toString(){
		return format(0);
	}
	String format(any* _ref_, int depth){
		var typeString=this.type.map(()->inner+" ").orElse("");
		var builder=new StringBuilder().append(typeString).append("{");
		var joiner=new StringJoiner(",");
		this.strings.entrySet().stream().map(()->createEntry(entry.getKey(), "\""+entry.getValue() + "\"", depth+1)).forEach(joiner::add);
		this.nodes.entrySet().stream().map(()->createEntry(entry.getKey(), entry.getValue().format(depth+1), depth+1)).forEach(joiner::add);
		this.nodeLists.entrySet().stream().map(()->createEntry(entry.getKey(), entry.getValue().stream().map(()->node.format(depth+1)).collect(Collectors.joining(",\n", "[", "]")), depth+1)).forEach(joiner::add);
		builder.append(joiner);
		return builder.append("\n").append("\t".repeat(depth)).append("}").toString();
	}
	Optional<Node> findNode(any* _ref_, String propertyKey){
		return Optional.ofNullable(this.nodes.get(propertyKey));
	}
	Node mapString(any* _ref_, String propertyKey, Tuple<any*, String (*)(any*, String)> mapper){
		return findString(propertyKey).map(mapper).map(()->withString(propertyKey, newString)).orElse(this);
	}
	Node merge(any* _ref_, Node other){
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
	<K, V>Stream<Tuple<K, V>> stream(any* _ref_, Map<K, V> map){
		return Streams.from(map.entrySet()).map(()->new Tuple<>(entry.getKey(), entry.getValue()));
	}
	String display(){
		return toString();
	}
	Node retype(any* _ref_, String type){
		return new MapNode(Optional.of(type), this.strings, this.nodes, this.nodeLists);
	}
	boolean is(any* _ref_, String type){
		return this.type.isPresent() && this.type.get().equals(type);
	}
	Node mapNodeList(any* _ref_, String propertyKey, Tuple<any*, List<Node> (*)(any*, List<Node>)> mapper){
		return findNodeList(propertyKey).map(mapper).map(()->withNodeList(propertyKey, list)).orElse(this);
	}
	boolean hasNodeList(any* _ref_, String propertyKey){
		return this.nodeLists.containsKey(propertyKey);
	}
	Node removeNodeList(any* _ref_, String propertyKey){
		var copy=new HashMap<>(this.nodeLists);
		copy.remove(propertyKey);
		return new MapNode(this.type, this.strings, this.nodes, copy);
	}
	Node mapNode(any* _ref_, String propertyKey, Tuple<any*, Node (*)(any*, Node)> mapper){
		return findNode(propertyKey).map(mapper).map(()->withNode(propertyKey, node)).orElse(this);
	}
	boolean hasNode(any* _ref_, String propertyKey){
		return this.nodes.containsKey(propertyKey);
	}
	boolean hasType(){
		return this.type.isPresent();
	}
	Node removeNode(any* _ref_, String propertyKey){
		var copy=new HashMap<>(this.nodes);
		copy.remove(propertyKey);
		return new MapNode(this.type, this.strings, copy, this.nodeLists);
	}
	Node withNode(any* _ref_, String propertyKey, Node propertyValue){
		var copy=new HashMap<>(this.nodes);
		copy.put(propertyKey, propertyValue);
		return new MapNode(this.type, this.strings, copy, this.nodeLists);
	}
	Node withNodeList(any* _ref_, String propertyKey, List<Node> propertyValues){
		var copy=new HashMap<>(this.nodeLists);
		copy.put(propertyKey, propertyValues);
		return new MapNode(this.type, this.strings, this.nodes, copy);
	}
	Optional<List<Node>> findNodeList(any* _ref_, String propertyKey){
		return Optional.ofNullable(this.nodeLists.get(propertyKey));
	}
	Node withString(any* _ref_, String propertyKey, String propertyValues){
		var copy=new HashMap<>(this.strings);
		copy.put(propertyKey, propertyValues);
		return new MapNode(this.type, copy, this.nodes, this.nodeLists);
	}
	Optional<String> findString(any* _ref_, String propertyKey){
		return Optional.ofNullable(this.strings.get(propertyKey));
	}
	Node N/A(){
		return N/A.new();
	}
}