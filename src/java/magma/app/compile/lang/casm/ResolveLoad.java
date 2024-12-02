package magma.app.compile.lang.casm;

import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.lang.casm.assemble.Operator;
import magma.app.compile.lang.magma.Stateless;

public class ResolveLoad implements Stateless {
    @Override
    public Node beforePass(Node node) {
        final var value = node.findNode("value").orElse(new MapNode());

        if (value.is("numeric-value")) {
            final var intValue = value.findInt("value").orElse(0);
            return CASMLang.instruct(Operator.LoadFromValue, intValue);
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
