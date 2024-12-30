package magma.compile.pass;

import magma.api.Tuple;
import magma.compile.Node;
import magma.compile.State;

public class Modifier implements Passer {
    @Override
    public Tuple<State, Node> afterPass(State state, Node node) {
        return Passer.super.afterPass(state, node);
    }
}
