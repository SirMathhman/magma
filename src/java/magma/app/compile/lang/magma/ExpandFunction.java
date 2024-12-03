package magma.app.compile.lang.magma;

import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.lang.common.CommonLang;
import magma.java.JavaList;

public class ExpandFunction implements Stateless {
    @Override
    public Node afterPass(Node node) {
        final var value = node.findNode("value").orElse(new MapNode());
        final var children = value.findNodeList("children").orElse(new JavaList<>());

        var functions = new JavaList<Node>();
        var statements = new JavaList<Node>();

        for (Node child : children.list()) {
            if (child.is("function")) {
                functions = functions.add(child);
            } else {
                statements = statements.add(child);
            }
        }

        final var withChildren = value.withNodeList("children", statements);
        final var newNode = node.withNode("value", withChildren);

        return CommonLang.asGroup(new JavaList<Node>()
                .addAll(functions)
                .add(newNode));
    }
}
