import magma.app.Node;struct NodeContext(Node node) implements Context{
	String display(){
		return node.display();
	}
	struct NodeContext new(){
		struct NodeContext this;
		return this;
	}
}