package magma.app.compile.lang.casm;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.casm.assemble.Operator;
import magma.app.compile.lang.common.CommonLang;
import magma.app.compile.lang.magma.Stateless;
import magma.java.JavaList;

import static magma.app.compile.Compiler.STACK_POINTER;

public class ResolveStore implements Stateless {
    private Node beforePass0(Node node) {
        final var value = node.findNode("value").orElse(new MapNode());

        if (value.is("address")) {
            final var offset = value.findInt("offset").orElse(0);
            return CommonLang.asGroup(new JavaList<Node>()
                    .add(new MapNode("move-stack-pointer").withInt("offset", offset))
                    .add(CASMLang.instruct(Operator.StoreIndirectly, STACK_POINTER))
                    .add(new MapNode("move-stack-pointer").withInt("offset", -offset)));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Result<Node, CompileError> beforePass(Node node) {
        return new Ok<>(beforePass0(node));
    }
}
