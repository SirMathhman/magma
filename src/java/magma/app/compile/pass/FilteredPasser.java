package magma.app.compile.pass;

import magma.app.compile.Node;

import java.util.Optional;

public record FilteredPasser(String type, Passer child) implements Passer {
    @Override
    public Optional<Node> beforePass(Node node) {
        if (node.is(type)) return child.beforePass(node);
        return Optional.empty();
    }

    @Override
    public Optional<Node> afterPass(Node node) {
        if (node.is(type)) return child.afterPass(node);
        return Optional.empty();
    }
}
