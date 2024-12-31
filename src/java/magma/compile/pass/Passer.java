package magma.compile.pass;

import magma.api.Tuple;
import magma.compile.Node;

public interface Passer<S> {
    default Tuple<S, Node> afterPass(S state, Node node) {
        return new Tuple<>(state, node);
    }

    default Tuple<S, Node> beforePass(S state, Node node) {
        return new Tuple<>(state, node);
    }
}
