package magma.app.compile.pass;

import magma.app.compile.Node;
import magma.java.NativeListCollector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record TreePassingStage(List<Passer> passers) implements PassingStage {
    private Node afterPass(Node node) {
        return passers.stream()
                .map(passer -> passer.afterPass(node))
                .flatMap(Optional::stream)
                .findFirst()
                .orElse(node);
    }

    @Override
    public Node pass(Node node) {
        final var withBefore = beforePass(node);
        final var withNodes = passNodes(withBefore);
        final var withNodeLists = passNodeLists(withNodes);
        return afterPass(withNodeLists);
    }

    private Node beforePass(Node node) {
        return passers.stream()
                .map(passer -> passer.beforePass(node))
                .flatMap(Optional::stream)
                .findFirst()
                .orElse(node);
    }

    private Node passNodeLists(Node withNodes) {
        var current = withNodes;
        final var nodeLists = withNodes.streamNodeLists().collect(new NativeListCollector<>());
        for (var tuple : nodeLists) {
            final var propertyKey = tuple.left();
            final var oldPropertyValues = tuple.right();
            var newPropertyValues = new ArrayList<Node>();
            for (Node oldPropertyValue : oldPropertyValues) {
                newPropertyValues.add(pass(oldPropertyValue));
            }

            current = current.withNodeList(propertyKey, newPropertyValues);
        }
        return current;
    }

    private Node passNodes(Node node) {
        final var nodes = node.streamNodes().collect(new NativeListCollector<>());
        var current = node;
        for (var tuple : nodes) {
            final var propertyKey = tuple.left();
            final var propertyValue = tuple.right();
            final var passed = pass(propertyValue);
            current = current.withNode(propertyKey, passed);
        }
        return current;
    }
}
