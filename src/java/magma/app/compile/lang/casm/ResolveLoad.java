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

import static magma.app.compile.Compiler.*;
import static magma.app.compile.lang.casm.CASMLang.instruct;

public class ResolveLoad implements Stateless {

    @Override
    public Result<Node, CompileError> beforePass(Node node) {
        final var value = node.findNode("value").orElse(new MapNode());

        if (value.is("numeric-value")) {
            final var intValue = value.findInt("value").orElse(0);
            var result = instruct(Operator.LoadFromValue, intValue);
            return new Ok<>(result);
        }
        if (value.is("address")) {
            final var offset = value.findInt("offset").orElse(0);
            var result = CommonLang.asGroup(new JavaList<Node>()
                    .add(new MapNode("move-stack-pointer").withInt("offset", offset))
                    .add(instruct(Operator.LoadIndirectly, STACK_POINTER))
                    .add(new MapNode("move-stack-pointer").withInt("offset", -offset)));
            return new Ok<>(result);
        }
        if (value.is("add")) {
            final var left = value.findNode("left").orElse(new MapNode());
            final var right = value.findNode("right").orElse(new MapNode());

            var group = CommonLang.asGroup(new JavaList<Node>()
                    .add(new MapNode("load").withNode("value", left))
                    .add(instruct(Operator.StoreDirectly, SPILL0))
                    .add(new MapNode("load").withNode("value", right))
                    .add(instruct(Operator.AddFromAddress, SPILL0)));

            return new Ok<>(group);
        }
        throw new UnsupportedOperationException(node.toString());
    }
}
