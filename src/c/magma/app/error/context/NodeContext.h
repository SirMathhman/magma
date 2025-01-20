package magma.app.error.context;package magma.app.Node;public record NodeContext(Node node) implements Context {@Override
    public String display(){return node.display();}}