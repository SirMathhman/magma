package magma.app.compile.lang.casm;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.common.CommonLang;
import magma.app.compile.lang.magma.Stateless;
import magma.java.JavaList;

public class ExpandAssign implements Stateless {
    private Node beforePass0(Node node) {
        final var type = node.findNode("type").orElse(new MapNode());
        final var length = type.findInt("length").orElse(0);

        final var source = node.findNode("source").orElse(new MapNode());
        final var destination = node.findNode("destination").orElse(new MapNode());

        JavaList<Node> nodes;
        if (length == 1) {
            nodes = new JavaList<Node>().add(new MapNode("move")
                    .withNode("source", source)
                    .withNode("destination", destination));
        } else {
            // for each "byte" go through each address and copy manually
            throw new UnsupportedOperationException();
        }

        return CommonLang.asGroup(nodes);
    }

    @Override
    public Result<Node, CompileError> beforePass(Node node) {
        return new Ok<>(beforePass0(node));
    }
}
