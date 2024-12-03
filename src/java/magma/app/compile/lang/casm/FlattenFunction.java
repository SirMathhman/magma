package magma.app.compile.lang.casm;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.magma.Stateless;
import magma.java.JavaList;

public class FlattenFunction implements Stateless {
    @Override
    public Node afterPass(Node node) {
        final var name = node.findString("name").orElse("");
        final var value = node.findNode("value").orElse(new MapNode());
        final var children = value.findNodeList("children")
                .orElse(new JavaList<Node>());

        return CASMLang.label(name, children.list());
    }

    private Node beforePass0(Node node) {
        return node;
    }

    @Override
    public Result<Node, CompileError> beforePass(Node node) {
        return new Ok<>(beforePass0(node));
    }
}
