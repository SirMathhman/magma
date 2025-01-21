import magma.app.Node;
struct NodeContext(Node node) implements Context {
	@Override
String display(){
		return node.display();
	}
}

