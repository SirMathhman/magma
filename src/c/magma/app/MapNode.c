import magma.api.Tuple;
import magma.api.stream.Stream;
import magma.api.stream.Streams;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
public final struct MapNode implements Node {
	private final Map<String, String> strings;
	private final Map<String, List<Node>> nodeLists;
	private final Map<String, Node> nodes;
	private final Optional<String> type;
	public MapNode=public MapNode(){
		this(Optional.empty(), new HashMap<>(), new HashMap<>(), new HashMap<>());
	};
	public MapNode=public MapNode(Optional<String> type, Map<String, String> strings, Map<String, Node> nodes, Map<String, List<Node>> nodeLists){
		this.type =type;
		this.strings =strings;
		this.nodes =nodes;
		this.nodeLists =nodeLists;
	};
	public MapNode=public MapNode(String type){
		this(Optional.of(type), new HashMap<>(), new HashMap<>(), new HashMap<>());
	};
	StringBuilder createEntry=StringBuilder createEntry(String name, String content, int depth){
		return new StringBuilder().append("\n"+"\t".repeat(depth)).append(name).append(" : ").append(content);
	};
	String toString=String toString(){
		return format(0);
	};
	String format=String format(int depth){
		final var typeString=this.type.map(inner ->inner+" ").orElse("");
		var builder=new StringBuilder().append(typeString).append("{");
		final var joiner=new StringJoiner(",");
		this.strings.entrySet().stream().map(entry -> createEntry(entry.getKey(), "\"" + entry.getValue() + "\"", depth + 1))
                .forEach(joiner::add);
		this.nodes.entrySet().stream().map(entry -> createEntry(entry.getKey(), entry.getValue().format(depth + 1), depth + 1))
                .forEach(joiner::add);
		this.nodeLists.entrySet().stream().map(entry -> createEntry(entry.getKey(), entry.getValue().stream().map(node ->node.format(depth+1)).collect(Collectors.joining(",\n", "[", "]")), depth + 1))
                .forEach(joiner::add);
		builder.append(joiner);
		return builder.append("\n").append("\t".repeat(depth)).append("}").toString();
	};
	Optional<Node> findNode=Optional<Node> findNode(String propertyKey){
		return Optional.ofNullable(this.nodes.get(propertyKey));
	};
	Node mapString=Node mapString(String propertyKey, ((String) => String) mapper){
		return findString(propertyKey).map(mapper).map(newString -> withString(propertyKey, newString)).orElse(this);
	};
	Node merge=Node merge(Node other){
		final var withStrings=stream(this.strings).foldLeft(other, (node, tuple) ->node.withString(tuple.left(), tuple.right()));
		final var withNodes=streamNodes().foldLeft(withStrings, (node, tuple) ->node.withNode(tuple.left(), tuple.right()));
		return streamNodeLists().foldLeft(withNodes, (node, tuple) ->node.withNodeList(tuple.left(), tuple.right()));
	};
	Stream<Tuple<String, List<Node>>> streamNodeLists=Stream<Tuple<String, List<Node>>> streamNodeLists(){
		return stream(this.nodeLists);
	};
	Stream<Tuple<String, Node>> streamNodes=Stream<Tuple<String, Node>> streamNodes(){
		return stream(this.nodes);
	};
	<K, V>Stream<Tuple<K, V>> stream=<K, V>Stream<Tuple<K, V>> stream(Map<K, V> map){
		return Streams.from(map.entrySet()).map(entry ->new Tuple<>(entry.getKey(), entry.getValue()));
	};
	String display=String display(){
		return toString();
	};
	Node retype=Node retype(String type){
		return new MapNode(Optional.of(type), this.strings, this.nodes, this.nodeLists);
	};
	boolean is=boolean is(String type){
		return this.type.isPresent() && this.type.get().equals(type);
	};
	Node mapNodeList=Node mapNodeList(String propertyKey, ((List<Node>) => List<Node>) mapper){
		return findNodeList(propertyKey).map(mapper).map(list -> withNodeList(propertyKey, list))
                .orElse(this);
	};
	boolean hasNodeList=boolean hasNodeList(String propertyKey){
		return this.nodeLists.containsKey(propertyKey);
	};
	Node removeNodeList=Node removeNodeList(String propertyKey){
		this.nodeLists.remove(propertyKey);
		return this;
	};
	Node mapNode=Node mapNode(String propertyKey, ((Node) => Node) mapper){
		return findNode(propertyKey).map(mapper).map(node -> withNode(propertyKey, node))
                .orElse(this);
	};
	Node withNode=Node withNode(String propertyKey, Node propertyValue){
		this.nodes.put(propertyKey, propertyValue);
		return this;
	};
	Node withNodeList=Node withNodeList(String propertyKey, List<Node> propertyValues){
		this.nodeLists.put(propertyKey, propertyValues);
		return this;
	};
	Optional<List<Node>> findNodeList=Optional<List<Node>> findNodeList(String propertyKey){
		return Optional.ofNullable(this.nodeLists.get(propertyKey));
	};
	Node withString=Node withString(String propertyKey, String propertyValues){
		this.strings.put(propertyKey, propertyValues);
		return this;
	};
	Optional<String> findString=Optional<String> findString(String propertyKey){
		return Optional.ofNullable(this.strings.get(propertyKey));
	};
}