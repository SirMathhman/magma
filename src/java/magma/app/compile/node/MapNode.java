package magma.app.compile.node;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.stream.JoiningCollector;
import magma.api.stream.Streams;
import magma.java.JavaList;

import java.util.function.Function;

public final class MapNode implements Node {
    private final NodeProperties<Input> inputs;
    private final NodeProperties<Node> nodes;
    private final NodeProperties<JavaList<Node>> nodeLists;
    private final Option<String> type;

    public MapNode(Option<String> type, NodeProperties<Input> inputs, NodeProperties<Node> nodes, NodeProperties<JavaList<Node>> nodeLists) {
        this.inputs = inputs;
        this.nodes = nodes;
        this.nodeLists = nodeLists;
        this.type = type;
    }

    public MapNode() {
        this(new None<>());
    }

    public MapNode(Option<String> type) {
        this.type = type;
        this.inputs = new MapNodeProperties<>(this::withInputs);
        this.nodes = new MapNodeProperties<>(this::withNodes);
        this.nodeLists = new MapNodeProperties<>(this::withNodeLists);
    }

    public MapNode(String type) {
        this(new Some<>(type));
    }

    private static String createEntry(String name, String content, int depth) {
        return createIndent(depth) + name + " : " + content;
    }

    private static String createIndent(int depth) {
        return "\n" + "\t".repeat(depth);
    }

    private static String joinNodeList(JavaList<Node> list, int depth) {
        return list.stream()
                .map(node -> node.format(depth + 1))
                .collect(new JoiningCollector(",\n"))
                .orElse("");
    }

    private Node withNodeLists(NodeProperties<JavaList<Node>> nodeLists) {
        return new MapNode(this.type, this.inputs, this.nodes, nodeLists);
    }

    private Node withNodes(NodeProperties<Node> nodes) {
        return new MapNode(this.type, this.inputs, nodes, this.nodeLists);
    }

    private Node withInputs(NodeProperties<Input> inputs) {
        return new MapNode(this.type, inputs, this.nodes, this.nodeLists);
    }

    @Override
    public String toString() {
        return format(0);
    }

    @Override
    public String format(int depth) {
        final var typeString = this.type.map(inner -> inner + " ").orElse("");
        return typeString + "{" + joinProperties(depth) + createIndent(depth) + "}";
    }

    private String joinProperties(int depth) {
        final var joinedInputs = joinPropertyMap(depth, this.inputs, input -> "\"" + input.unwrap() + "\"");
        final var joinedNodes = joinPropertyMap(depth, this.nodes, node -> node.format(depth + 1));
        final var joinedNodeLists = joinPropertyMap(depth, this.nodeLists, nodeList -> "[" + joinNodeList(nodeList, depth) + "]");

        return Streams.fromOption(joinedInputs)
                .concat(Streams.fromOption(joinedNodes))
                .concat(Streams.fromOption(joinedNodeLists))
                .collect(new JoiningCollector(","))
                .orElse("");
    }

    private <T> Option<String> joinPropertyMap(
            int depth,
            NodeProperties<T> properties,
            Function<T, String> mapper
    ) {
        return properties.stream()
                .map(entry -> createEntry(entry.left(), mapper.apply(entry.right()), depth + 1))
                .collect(new JoiningCollector(","));
    }

    @Override
    public Node merge(Node other, MergeStrategy strategy) {
        final var newInputs = this.inputs.merge(other.inputs(), strategy);
        final var newNodes = this.nodes.merge(other.nodes(), strategy);
        final var newNodeLists = this.nodeLists.merge(other.nodeLists(), strategy);
        final var newType = other.mergeType(this.type, strategy);
        return new MapNode(newType, newInputs, newNodes, newNodeLists);
    }

    @Override
    public String display() {
        return toString();
    }

    @Override
    public Node retype(String type) {
        return new MapNode(new Some<>(type), this.inputs, this.nodes, this.nodeLists);
    }

    @Override
    public boolean is(String type) {
        return this.type.filter(inner -> inner.equals(type)).isPresent();
    }

    @Override
    public boolean hasType() {
        return this.type.isPresent();
    }

    @Override
    public NodeProperties<Input> inputs() {
        return this.inputs;
    }

    @Override
    public NodeProperties<Node> nodes() {
        return this.nodes;
    }

    @Override
    public NodeProperties<JavaList<Node>> nodeLists() {
        return this.nodeLists;
    }

    @Override
    public Option<String> mergeType(Option<String> otherType, MergeStrategy strategy) {
        if (otherType.isPresent()) {
            if (this.type.isPresent()) {
                return otherType.and(() -> this.type)
                        .map(tuple -> strategy.merge(tuple.left(), tuple.right()));
            }
        }
        if (otherType.isPresent()) {
            return otherType;
        }
        if (this.type.isPresent()) {
            return this.type;
        }
        return new None<>();
    }
}
