package magma.app.compile;

import magma.app.compile.lang.common.CommonLang;
import magma.app.compile.lang.magma.Stateless;
import magma.java.JavaList;

public class ExpandDeclare implements Stateless {
    @Override
    public Node beforePass(Node node) {
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

        return CommonLang.toGroup(new JavaList<Node>()
                .add(define)
                .add(assign));
    }
}
