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
        int i = 0;
        while (i < nodeLists.size()) {
            var tuple = nodeLists.get(i);
            final var propertyKey = tuple.left();
            final var oldPropertyValues = tuple.right();
            var newPropertyValues = new ArrayList<Node>();
            int j = 0;
            while (j < oldPropertyValues.size()) {
                Node oldPropertyValue = oldPropertyValues.get(j);
                newPropertyValues.add(pass(oldPropertyValue));
                j++;
            }

            current = current.withNodeList(propertyKey, newPropertyValues);
            i++;
        }
        return current;
    }

    private Node passNodes(Node node) {
        final var nodes = node.streamNodes().collect(new NativeListCollector<>());
        var current = node;
        int i = 0;
        while (i < nodes.size()) {
            var tuple = nodes.get(i);
            final var propertyKey = tuple.left();
            final var propertyValue = tuple.right();
            final var passed = pass(propertyValue);
            current = current.withNode(propertyKey, passed);
            i++;
        }
        return current;
    }
}
