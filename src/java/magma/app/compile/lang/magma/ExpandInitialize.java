package magma.app.compile.lang.magma;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.common.CommonLang;
import magma.java.JavaList;

public class ExpandInitialize implements Stateless {
    private Node beforePass0(Node node) {
        final var name = node.findString("name").orElse("");
        final var type = node.findNode("type").orElse(new MapNode());
        final var value = node.findNode("value").orElse(new MapNode());

        final var define = new MapNode("define")
                .withString("name", name)
                .withNode("type", type);

        final var symbol = new MapNode("symbol")
                .withString("value", name);

        final var assign = new MapNode("assign")
                .withNode("type", type)
                .withNode("source", value)
                .withNode("destination", symbol);

        return CommonLang.asGroup(new JavaList<Node>()
                .add(define)
                .add(assign));
    }

    @Override
    public Result<Node, CompileError> beforePass(Node node) {
        return new Ok<>(beforePass0(node));
    }
}
