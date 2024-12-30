package magma.compile.pass;

import magma.compile.State;
import magma.api.Tuple;
import magma.compile.Node;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Modifier implements Passer {
    private static Node modifyStateless(Node node) {
        if (node.is("group")) {
            final var oldChildren = node.findNodeList("children").orElse(new ArrayList<>());
            final var newChildren = oldChildren.stream()
                    .filter(oldChild -> !oldChild.is("package"))
                    .collect(Collectors.toCollection(ArrayList::new));

            return node.withNodeList("children", newChildren);
        } else if (node.is("class")) {
            return node.retype("struct");
        } else if (node.is("import")) {
            return node.retype("include");
        } else if (node.is("method")) {
            return node.retype("function");
        } else {
            return node;
        }
    }

    @Override
    public Tuple<State, Node> afterPass(State state, Node node) {
        final var result = modifyStateless(node);
        return new Tuple<>(state, result);
    }
}
