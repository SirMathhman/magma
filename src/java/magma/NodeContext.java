package magma;

import magma.error.Context;

public record NodeContext(Node node) implements Context  {
    @Override
    public String display() {
        return node.display();
    }
}
