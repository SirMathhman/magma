import magma.api.Tuple;
import magma.api.stream.Stream;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
public struct Node {Node withNodeList(String propertyKey List<Node> propertyValues);Optional<List<Node>> findNodeList(String propertyKey);Node withString(String propertyKeyString propertyValue);Optional<String> findString(String propertyKey);Node withNode(String propertyKeyNode propertyValue);Optional<Node> findNode(String propertyKey);Node mapString(String propertyKey Function<StringString> mapper);Node merge(Node other);Stream<Tuple<String List<Node>>> streamNodeLists();Stream<Tuple<StringNode>> streamNodes();String display();Node retype(String type);boolean is(String type);}