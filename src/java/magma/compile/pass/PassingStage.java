package magma.compile.pass;

import magma.api.Tuple;
import magma.compile.Node;

public interface PassingStage<S> {
    Tuple<S, Node> pass(
            S state,
            Node node);
}
