package magma.app.compile.lang.magma;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.casm.assemble.Operator;
import magma.app.compile.lang.common.CommonLang;
import magma.java.JavaList;

import static magma.app.compile.Compiler.SPILL0;
import static magma.app.compile.lang.casm.CASMLang.instruct;

public class ResolveLessThan implements Stateless {
    @Override
    public Result<Node, CompileError> beforePass(Node node) {
        final var left = node.findNode("left").orElse(new MapNode());
        final var right = node.findNode("right").orElse(new MapNode());

        var group = CommonLang.asGroup(new JavaList<Node>()
                .add(right)
                .add(instruct(Operator.StoreDirectly, SPILL0))
                .add(left)
                .add(instruct(Operator.LessThanAddress, SPILL0)));

        return new Ok<>(group);
    }
}
