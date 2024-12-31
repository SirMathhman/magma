package magma.compile.pass;

import magma.compile.State;
import magma.api.Tuple;
import magma.compile.Node;

import java.util.ArrayList;
import java.util.List;

public record TreePassingStage(Passer<State> passer) {
    public Tuple<State, Node> pass(
            State state,
            Node node) {
        final var withBefore = passer.beforePass(state, node);
        final var withNodeLists = withBefore.right()
                .streamNodeLists()
                .reduce(withBefore, this::passNodeLists, (_, next) -> next);

        final var withNodes = withNodeLists.right()
                .streamNodes()
                .reduce(withNodeLists, this::passNode, (_, next) -> next);

        return passer.afterPass(withNodes.left(), withNodes.right());
    }

    public Tuple<State, Node> passNodeLists(Tuple<State, Node> node1, Tuple<String, List<Node>> tuple) {
        final var oldState = node1.left();
        final var oldChildren = node1.right();

        final var key = tuple.left();
        final var values = tuple.right();

        var currentState = oldState;
        var currentChildren = new ArrayList<Node>();
        int i = 0;
        while (i < values.size()) {
            Node value = values.get(i);
            final var passed = pass(currentState, value);

            currentState = passed.left();
            currentChildren.add(passed.right());
            i = i + 1;
        }

        final var newNode = oldChildren.withNodeList(key, currentChildren);
        return new Tuple<>(oldState, newNode);
    }

    public Tuple<State, Node> passNode(Tuple<State, Node> node1, Tuple<String, Node> tuple) {
        final var oldState = node1.left();
        final var oldNode = node1.right();

        final var key = tuple.left();
        final var value = tuple.right();

        return pass(oldState, value).mapRight(right -> oldNode.withNode(key, right));
    }
}