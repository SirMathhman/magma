package magma.app.compile.lang.magma;

import magma.app.compile.Node;

public interface Stateless {
    default Node beforePass(Node node) {
        return node;
    }

    default Node afterPass(Node node) {
        return node;
    }
}
