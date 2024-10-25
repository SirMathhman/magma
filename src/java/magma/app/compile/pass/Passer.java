package magma.app.compile.pass;

import magma.app.compile.Node;

import java.util.Optional;

public interface Passer {
    default Optional<Node> beforePass(Node node) {
        return Optional.empty();
    }

    default Optional<Node> afterPass(Node node) {
        return Optional.empty();
    }
}
