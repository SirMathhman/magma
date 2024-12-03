package magma.app.compile.lang.casm;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.common.CommonLang;
import magma.app.compile.lang.magma.Stateless;
import magma.java.JavaList;

public class ExpandMove implements Stateless {
    private Node beforePass0(Node node) {
        final var source = node.findNode("source").orElse(new MapNode());
        final var destination = node.findNode("destination").orElse(new MapNode());

        final var store = new MapNode("store").withNode("value", destination);
        return CommonLang.asGroup(new JavaList<Node>().add(source).add(store));
    }

    @Override
    public Result<Node, CompileError> beforePass(Node node) {
        return new Ok<>(beforePass0(node));
    }
}
