package magma.error;

import magma.Node;

public record NodeContext(Node node) implements Context {
    @Override
    public String display() {
        return node.display();
    }
}
