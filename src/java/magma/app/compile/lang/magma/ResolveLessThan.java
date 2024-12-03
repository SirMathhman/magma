package magma.app.compile.lang.magma;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.common.CommonLang;
import magma.java.JavaList;

public class ResolveLessThan implements Stateless {
    @Override
    public Result<Node, CompileError> beforePass(Node node) {
        final var left = node.findNode("left").orElse(new MapNode());
        final var right = node.findNode("right").orElse(new MapNode());

        var group = CommonLang.asGroup(new JavaList<Node>().add(new MapNode("subtract")
                .withNode("left", right)
                .withNode("right", left)));

        return new Ok<>(group);
    }
}
