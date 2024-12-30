package magma.compile.pass;

import magma.api.Tuple;
import magma.compile.Node;
import magma.compile.State;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Modifier implements Passer {
    private static Node modifyStateless(Node node) {
        if (node.is("group")) {
            final var oldChildren = node.findNodeList("children").orElse(new ArrayList<>());
            final var newChildren = oldChildren.stream()
                    .filter(oldChild -> !oldChild.is("package"))
                    .collect(Collectors.toCollection(ArrayList::new));

            return node.withNodeList("children", newChildren);
        } else if (node.is("class")) {
            return node.retype("struct");
        } else if (node.is("import")) {
            return node.retype("include");
        } else if (node.is("method")) {
            return node.retype("function");
        } else if (node.is("invocation-statement") || node.is("invocation-value")) {
            final var caller = node.findNode("caller").orElse(new Node());
            final var arguments = node.findNodeList("arguments").orElse(new ArrayList<>());

            if (caller.is("data-access")) {
                final var object = caller.findNode("object").orElse(new Node());
                final var property = caller.findString("property").orElse("");

                if(object.is("symbol")) {
                    return node;
                }

                final var symbol = new Node("symbol")
                        .withString("symbol-value", "local");
                final var newArguments = new ArrayList<Node>();
                newArguments.add(symbol);
                newArguments.addAll(arguments);

                final var children = List.of(
                        new Node("initialization")
                                .withNode("definition", new Node("definition")
                                        .withString("name", "local")
                                        .withNode("type", new Node("symbol").withString("symbol-value", "auto"))
                                ).withNode("value", object),
                        new Node("invocation-value")
                                .withNode("caller", new Node("data-access")
                                        .withNode("object", symbol)
                                        .withString("property", property))
                                .withNodeList("arguments", newArguments)
                );

                final var group = new Node("group").withNodeList("children", children);
                return new Node("block").withNode("value", group);
            } else {
                return node;
            }
        } else {
            return node;
        }
    }

    @Override
    public Tuple<State, Node> afterPass(State state, Node node) {
        final var result = modifyStateless(node);
        return new Tuple<>(state, result);
    }
}
