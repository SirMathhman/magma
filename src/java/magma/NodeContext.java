package magma;

import magma.app.Context;

public record NodeContext(MapNode node) implements Context {
    @Override
    public String display() {
        return node.display();
    }
}
