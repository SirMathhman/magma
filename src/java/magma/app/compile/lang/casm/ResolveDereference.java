package magma.app.compile.lang.casm;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;
import magma.app.compile.lang.casm.assemble.Operator;
import magma.app.compile.lang.common.CommonLang;
import magma.app.compile.lang.magma.Stateless;
import magma.java.JavaList;

import static magma.app.compile.Compiler.STACK_POINTER;
import static magma.app.compile.lang.casm.CASMLang.instruct;

public class ResolveDereference implements Stateless {
    @Override
    public Result<Node, CompileError> beforePass(Node node) {
        final var inner = node.findNode("value").orElse(new MapNode());
        if (!inner.is("address")) return new Err<>(new CompileError("Cannot resolve address", new NodeContext(node)));

        final var offset = node.findInt("offset").orElse(0);
        var result = CommonLang.asGroup(new JavaList<Node>()
                .add(new MapNode("move-stack-pointer").withInt("offset", offset))
                .add(instruct(Operator.LoadIndirectly, STACK_POINTER))
                .add(new MapNode("move-stack-pointer").withInt("offset", -offset)));

        return new Ok<>(result);
    }
}
