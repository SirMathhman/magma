import magma.app.Node;struct NodeContext(Node node) implements Context{
	String display(){
		return node.display();
	}
	Context N/A(){
		return N/A.new();
	}
}