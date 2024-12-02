package magma.app.compile.lang.casm;

import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.lang.magma.Stateless;
import magma.java.JavaList;

public class FlattenFunction implements Stateless {
    @Override
    public Node beforePass(Node node) {
        final var name = node.findString("name").orElse("");
        final var value = node.findNode("value").orElse(new MapNode());
        final var children = value.findNodeList("children").orElse(new JavaList<Node>());
        return CASMLang.label(name, children.list());
    }
}
