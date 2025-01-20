import magma.app.Node;
public struct NodeContext(Node node) implements Context {
	(() => String) display=String display(){
		return node.display();
	};
}