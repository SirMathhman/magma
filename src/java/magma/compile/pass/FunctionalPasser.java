package magma.compile.pass;

import magma.compile.State;
import magma.api.Tuple;
import magma.compile.Node;

import java.util.function.BiFunction;

public record FunctionalPasser(
        BiFunction<State, Node, Tuple<State, Node>> beforePass,
        BiFunction<State, Node, Tuple<State, Node>> afterPass
) implements Passer {
    @Override
    public Tuple<State, Node> afterPass(State state, Node node) {
        return afterPass().apply(state, node);
    }

    @Override
    public Tuple<State, Node> beforePass(State state, Node node) {
        return beforePass().apply(state, node);
    }
}