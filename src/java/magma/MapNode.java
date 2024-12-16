package magma;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

public record MapNode(Option<String> type, Map<String, String> strings,
                      Map<String, List<Node>> nodeLists) implements Node {
    public MapNode() {
        this(new None<>(), new MutableMap<>(), new MutableMap<>());
    }

    @Override
    public Node retype(String type) {
        return new MapNode(new Some<>(type), strings, nodeLists);
    }

    @Override
    public boolean is(String type) {
        return this.type.filter(value -> value.equals(type)).isPresent();
    }

    @Override
    public String display() {
        return toString();
    }

    @Override
    public Node merge(Node node) {
        final var withStrings =
                node.streamStrings().<Node>foldLeft(this, (current, tuple) -> current.withString(tuple.left(), tuple.right()));

        return node.streamNodeLists()
                .foldLeft(withStrings, (current, tuple) -> current.withNodeList(tuple.left(), tuple.right()));
    }

    @Override
    public MapNode withNodeList(String propertyKey, List<Node> propertyValues) {
        return new MapNode(type, strings, nodeLists.put(propertyKey, propertyValues));
    }

    @Override
    public Stream<Tuple<String, String>> streamStrings() {
        return strings.stream();
    }

    @Override
    public Node withString(String propertyKey, String propertyValue) {
        return new MapNode(type, strings.put(propertyKey, propertyValue), nodeLists);
    }

    @Override
    public Stream<Tuple<String, List<Node>>> streamNodeLists() {
        return nodeLists.stream();
    }
}
