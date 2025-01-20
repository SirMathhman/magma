import magma.api.Tuple;
import magma.api.stream.Stream;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
public struct Node {
	((String, List<Node>) => Node) withNodeList;
	((String) => Optional<List<Node>>) findNodeList;
	((String, String) => Node) withString;
	((String) => Optional<String>) findString;
	((String, Node) => Node) withNode;
	((int) => String) format;
	((String) => Optional<Node>) findNode;
	((String, [Capture, ((Capture, String) => String)]) => Node) mapString;
	((Node) => Node) merge;
	(() => Stream<[String, List<Node>]>) streamNodeLists;
	(() => Stream<[String, Node]>) streamNodes;
	(() => String) display;
	((String) => Node) retype;
	((String) => boolean) is;
	((String, [Capture, ((Capture, List<Node>) => List<Node>)]) => Node) mapNodeList;
	((String) => boolean) hasNodeList;
	((String) => Node) removeNodeList;
	((String, [Capture, ((Capture, Node) => Node)]) => Node) mapNode;
}