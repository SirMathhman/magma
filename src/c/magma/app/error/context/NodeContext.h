import magma.app.Node;
public struct NodeContext(Node node) implements Context {@Override
    public String display(){return node.display();}}