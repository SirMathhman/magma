package magma;

import magma.api.Tuple;
import magma.compile.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Formatter implements Passer {
    private static Node attachIndent(int depth, int index, Node child) {
        if (depth == 0 && index == 0) return child;
        final var indent = "\n" + "\t".repeat(depth);
        return child.withString("before-child", indent);
    }

    @Override
    public Tuple<State, Node> beforePass(State state, Node node) {
        if (node.is("block")) {
            return new Tuple<>(state.enter(), node);
        }

        return new Tuple<>(state, node);
    }

    @Override
    public Tuple<State, Node> afterPass(State state, Node node) {
        if (node.is("group")) {
            final var oldChildren = node.findNodeList("children");
            final var newChildren = new ArrayList<Node>();
            List<Node> orElse = oldChildren.orElse(Collections.emptyList());
            int i = 0;
            while (i < orElse.size()) {
                Node child = orElse.get(i);
                final var withString = attachIndent(state.depth(), i, child);
                newChildren.add(withString);
                i = i + 1;
            }

            return new Tuple<>(state, node
                    .withNodeList("children", newChildren)
                    .withString("after-children", "\n" + "\t".repeat(Math.max(state.depth() - 1, 0))));
        } else if (node.is("block")) {
            return new Tuple<>(state.exit(), node);
        } else {
            return new Tuple<>(state, node);
        }
    }
}
