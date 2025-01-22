import magma.api.Tuple;import magma.api.stream.Stream;import magma.api.stream.Streams;import java.util.HashMap;import java.util.List;import java.util.Map;import java.util.Optional;import java.util.StringJoiner;import java.util.function.Function;import java.util.stream.Collectors;struct MapNode implements Node {
const Map<String, String> strings;
const Map<String, List<Node>> nodeLists;
const Map<String, Node> nodes;
const Optional<String> type;
}