package magma;

public class NodeContext implements Context {
    private final Node node;

    public NodeContext(Node node) {
        this.node = node;
    }

    @Override
    public String display() {
        return node.toString();
    }
}
