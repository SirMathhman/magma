package magma.app.compile.lang.casm;

import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.lang.casm.assemble.Operator;
import magma.app.compile.lang.common.CommonLang;
import magma.app.compile.lang.magma.Stateless;
import magma.java.JavaList;

import static magma.app.compile.Compiler.STACK_POINTER;

public class ResolveStore implements Stateless {
    @Override
    public Node beforePass(Node node) {
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
}
