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
	public MapNode(){
		this(Optional.empty(), new HashMap<>(), new HashMap<>(), new HashMap<>());
	}
	public MapNode(Optional<String> type,  Map<String, String> strings,  Map<String, Node> nodes,  Map<String, List<Node>> nodeLists){
		this.type =type;
		this.strings =strings;
		this.nodes =nodes;
		this.nodeLists =nodeLists;
	}
	public MapNode(String type){
		this(Optional.of(type), new HashMap<>(), new HashMap<>(), new HashMap<>());
	}
	private static StringBuilder createEntry(String name,  String content,  int depth){
		return new StringBuilder()                .append("\n" + "\t".repeat(depth))                .append(name)                .append(" : ")  .append(content);
	}
	@Override    public String toString(){
		return format(0);
	}
	@Override    public String format(int depth);
	@Override    public Optional<Node> findNode(String propertyKey){
		return Optional.ofNullable(this.nodes.get(propertyKey));
	}
	@Override    public Node mapString(String propertyKey,  ((String) => String) mapper){
		return findString(propertyKey).map(mapper).map(newString -> withString(propertyKey, newString)).orElse(this);
	}
	@Override    public Node merge(Node other){
		final var withStrings =stream(this.strings).foldLeft(other,  (node, tuple) ->node.withString(tuple.left(), tuple.right()));
		final var withNodes =streamNodes().foldLeft(withStrings,  (node, tuple) ->node.withNode(tuple.left(), tuple.right()));
		return streamNodeLists().foldLeft(withNodes,  (node, tuple) ->node.withNodeList(tuple.left(), tuple.right()));
	}
	@Override    public Stream<Tuple<String, List<Node>>> streamNodeLists(){
		return stream(this.nodeLists);
	}
	@Override    public Stream<Tuple<String, Node>> streamNodes(){
		return stream(this.nodes);
	}
	private <K, V> Stream<Tuple<K, V>> stream(Map<K, V> map){
		return Streams.from(map.entrySet()).map(entry ->new Tuple<>(entry.getKey(), entry.getValue()));
	}
	@Override    public String display(){
		return toString();
	}
	@Override    public Node retype(String type){
		return new MapNode(Optional.of(type), this.strings, this.nodes, this.nodeLists);
	}
	@Override    public boolean is(String type){
		return this.type.isPresent() && this.type.get().equals(type);
	}
	@Override    public Node mapNodeList(String propertyKey,  ((List<Node>) => List<Node>) mapper){
		return findNodeList(propertyKey)                .map(mapper)                .map(list -> withNodeList(propertyKey, list))  .orElse(this);
	}
	@Override    public Node withNode(String propertyKey,  Node propertyValue){
		this.nodes.put(propertyKey, propertyValue);
		return this;
	}
	@Override    public Node withNodeList(String propertyKey,  List<Node> propertyValues){
		this.nodeLists.put(propertyKey, propertyValues);
		return this;
	}
	@Override    public Optional<List<Node>> findNodeList(String propertyKey){
		return Optional.ofNullable(this.nodeLists.get(propertyKey));
	}
	@Override    public Node withString(String propertyKey,  String propertyValues){
		this.strings.put(propertyKey, propertyValues);
		return this;
	}
	@Override    public Optional<String> findString(String propertyKey){
		return Optional.ofNullable(this.strings.get(propertyKey));
	}
}