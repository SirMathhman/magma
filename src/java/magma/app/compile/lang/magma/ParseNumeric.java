package magma.app.compile.lang.magma;

import magma.app.compile.MapNode;
import magma.app.compile.Node;

public class ParseNumeric implements Stateless {
    @Override
    public Node afterPass(Node node) {
        final var value = node.findString("value").orElse("");
        final var sign = value.charAt(0) == 'I' ? 1 : 0;
        final var bits = Integer.parseInt(value.substring(1));

        return new MapNode("numeric-type")
                .withInt("length", 1)
                .withInt("signed", sign)
                .withInt("bits", bits);
    }
}