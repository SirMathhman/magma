import magma.app.Node;

@Override
String display(){
	return node.display();
}
struct NodeContext(Node node) implements Context {
}

