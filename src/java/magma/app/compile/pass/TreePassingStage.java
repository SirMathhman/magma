package magma.app.compile.pass;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.ResultStream;
import magma.app.compile.error.CompileError;
import magma.app.compile.Node;
import magma.java.JavaList;

public class TreePassingStage implements PassingStage {
    private final Passer passer;

    public TreePassingStage(Passer passer) {
        this.passer = passer;
    }

    public Result<Node, CompileError> passNodeLists(Node withNodes) {
        return withNodes.streamNodeLists().foldLeftToResult(withNodes, (node, tuple) -> {
            final var propertyKey = tuple.left();
            final var propertyValues = tuple.right();
            return propertyValues.stream()
                    .map(this::pass)
                    .into(ResultStream::new)
                    .foldResultsLeft(new JavaList<Node>(), JavaList::add)
                    .mapValue(newValues -> node.withNodeList(propertyKey, newValues));
        });
    }

    public Result<Node, CompileError> passNodes(Node root) {
        return root.streamNodes().foldLeftToResult(root, (node, tuple) -> {
            final var propertyKey = tuple.left();
            final var propertyValue = tuple.right();
            return pass(propertyValue).mapValue(newValue -> node.withNode(propertyKey, newValue));
        });
    }

    @Override
    public Result<Node, CompileError> pass(Node root) {
        return passer.beforeNode(root).orElse(new Ok<>(root))
                .flatMapValue(this::passNodes)
                .flatMapValue(this::passNodeLists)
                .flatMapValue(node -> passer.afterNode(node).orElse(new Ok<>(node)));
    }
}