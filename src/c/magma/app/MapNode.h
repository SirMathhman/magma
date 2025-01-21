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
final struct MapNode implements Node {
	const Map<String, String> strings;
	const Map<String, List<Node>> nodeLists;
	const Map<String, Node> nodes;
	const Optional<String> type;
	public MapNode(){
		this(Optional.empty(), new HashMap<>(), new HashMap<>(), new HashMap<>());
	}
	public MapNode(Optional<String> type, Map<String, String> strings, Map<String, Node> nodes, Map<String, List<Node>> nodeLists){
		this.type =type;
		this.strings =strings;
		this.nodes =nodes;
		this.nodeLists =nodeLists;
	}
	public MapNode(String type){
		this(Optional.of(type), new HashMap<>(), new HashMap<>(), new HashMap<>());
	}
	static StringBuilder createEntry(String name, String content, int depth){
		return new StringBuilder().append("\n"+"\t".repeat(depth)).append(name).append(" : ").append(content);
	}
	@Override
String toString(){
		return format(0);
	}
	@Override
String format(int depth){
		const var typeString=this.type.map(inner ->inner+" ").orElse("");
		var builder=new StringBuilder().append(typeString).append("{");
		const var joiner=new StringJoiner(",");
		this.strings.entrySet().stream().map(entry -> createEntry(entry.getKey(), "\"" + entry.getValue() + "\"", depth + 1))
                .forEach(joiner::add);
		this.nodes.entrySet().stream().map(entry -> createEntry(entry.getKey(), entry.getValue().format(depth + 1), depth + 1))
                .forEach(joiner::add);
		this.nodeLists.entrySet().stream().map(entry -> createEntry(entry.getKey(), entry.getValue().stream().map(node ->node.format(depth+1)).collect(Collectors.joining(",\n", "[", "]")), depth + 1))
                .forEach(joiner::add);
		builder.append(joiner);
		return builder.append("\n").append("\t".repeat(depth)).append("}").toString();
	}
	@Override
Optional<Node> findNode(String propertyKey){
		return Optional.ofNullable(this.nodes.get(propertyKey));
	}
	@Override
Node mapString(String propertyKey, Function<String, String> mapper){
		return findString(propertyKey).map(mapper).map(newString -> withString(propertyKey, newString)).orElse(this);
	}
	@Override
Node merge(Node other){
		const var withStrings=stream(this.strings).foldLeft(other, (node, tuple) ->node.withString(tuple.left(), tuple.right()));
		const var withNodes=streamNodes().foldLeft(withStrings, (node, tuple) ->node.withNode(tuple.left(), tuple.right()));
		return streamNodeLists().foldLeft(withNodes, (node, tuple) ->node.withNodeList(tuple.left(), tuple.right()));
	}
	@Override
Stream<Tuple<String, List<Node>>> streamNodeLists(){
		return stream(this.nodeLists);
	}
	@Override
Stream<Tuple<String, Node>> streamNodes(){
		return stream(this.nodes);
	}
	<K, V>Stream<Tuple<K, V>> stream(Map<K, V> map){
		return Streams.from(map.entrySet()).map(entry ->new Tuple<>(entry.getKey(), entry.getValue()));
	}
	@Override
String display(){
		return toString();
	}
	@Override
Node retype(String type){
		return new MapNode(Optional.of(type), this.strings, this.nodes, this.nodeLists);
	}
	@Override
boolean is(String type){
		return this.type.isPresent() && this.type.get().equals(type);
	}
	@Override
Node mapNodeList(String propertyKey, Function<List<Node>, List<Node>> mapper){
		return findNodeList(propertyKey).map(mapper).map(list -> withNodeList(propertyKey, list))
                .orElse(this);
	}
	@Override
boolean hasNodeList(String propertyKey){
		return this.nodeLists.containsKey(propertyKey);
	}
	@Override
Node removeNodeList(String propertyKey){
		const var copy=new HashMap<>(this.nodeLists);
		copy.remove(propertyKey);
		return new MapNode(this.type, this.strings, this.nodes, copy);
	}
	@Override
Node mapNode(String propertyKey, Function<Node, Node> mapper){
		return findNode(propertyKey).map(mapper).map(node -> withNode(propertyKey, node))
                .orElse(this);
	}
	@Override
Node withNode(String propertyKey, Node propertyValue){
		const var copy=new HashMap<>(this.nodes);
		copy.put(propertyKey, propertyValue);
		return new MapNode(this.type, this.strings, copy, this.nodeLists);
	}
	@Override
Node withNodeList(String propertyKey, List<Node> propertyValues){
		const var copy=new HashMap<>(this.nodeLists);
		copy.put(propertyKey, propertyValues);
		return new MapNode(this.type, this.strings, this.nodes, copy);
	}
	@Override
Optional<List<Node>> findNodeList(String propertyKey){
		return Optional.ofNullable(this.nodeLists.get(propertyKey));
	}
	@Override
Node withString(String propertyKey, String propertyValues){
		const var copy=new HashMap<>(this.strings);
		copy.put(propertyKey, propertyValues);
		return new MapNode(this.type, copy, this.nodes, this.nodeLists);
	}
	@Override
Optional<String> findString(String propertyKey){
		return Optional.ofNullable(this.strings.get(propertyKey));
	}
}

