package magma.compile.error;

import magma.compile.Node;

public record NodeContext(Node node) implements Context {
    @Override
    public String asString() {
        return node.asString();
    }
}
