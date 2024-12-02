package magma.app.compile.lang.casm;

import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.lang.common.CommonLang;
import magma.app.compile.lang.magma.Stateless;
import magma.java.JavaList;

public class ExpandMove implements Stateless {
    @Override
    public Node beforePass(Node node) {
        final var source = node.findNode("source").orElse(new MapNode());
        final var destination = node.findNode("destination").orElse(new MapNode());

        final var load = new MapNode("load").withNode("value", source);
        final var store = new MapNode("store").withNode("value", destination);
        return CommonLang.asGroup(new JavaList<Node>().add(load).add(store));
    }
}
