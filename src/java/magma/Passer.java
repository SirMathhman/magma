package magma;

import magma.api.Tuple;
import magma.compile.Node;

public interface Passer {
    default Tuple<State, Node> afterPass(State state, Node node) {
        return new Tuple<>(state, node);
    }

    default Tuple<State, Node> beforePass(State state, Node node) {
        return new Tuple<>(state, node);
    }
}
