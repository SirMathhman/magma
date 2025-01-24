import magma.app.Node;struct NodeContext(Node node) implements Context{
	struct Table{
		String display(){
			return node.display();
		}
	}
	struct Impl{}
}