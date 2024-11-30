package magma.app.compile.error;

import magma.app.compile.Node;

public record NodeContext(Node node) implements Context {
    @Override
    public String display() {
        return node.display();
    }
}
