package magma.app.compile.pass;

import magma.app.compile.Node;

import java.util.Optional;

public interface Passer {
    Optional<Node> beforePass(Node node);

    Optional<Node> afterPass(Node node);
}
