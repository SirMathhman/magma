package magma.compile.pass;

import magma.api.Tuple;
import magma.compile.Node;
import magma.compile.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Modifier implements Passer {
    private int counter = 0;

    private Node modifyStateless(Node node) {
        if (node.is("group")) {
            final var oldChildren = node.findNodeList("children").orElse(new ArrayList<>());
            final var newChildren = oldChildren.stream()
                    .filter(oldChild -> !oldChild.is("package"))
                    .collect(Collectors.toCollection(ArrayList::new));

            return node.withNodeList("children", newChildren);
        }
        if (node.is("class")) {
            return node.retype("struct");
        }
        if (node.is("import")) {
            return node.retype("include");
        }
        if (node.is("method")) {
            return node.retype("function");
        }

        return modifyInvocation(node)
                .or(() -> modifyFunctionAccess(node))
                .or(() -> modifyArray(node))
                .orElse(node);
    }

    private Optional<? extends Node> modifyArray(Node node) {
        if(node.is("array")) {
            final var child = node.findNode("child").orElse(new Node());
            return Optional.of(new Node("generic")
                    .withString("parent", "Array")
                    .withNodeList("children", List.of(child)));
        } else {
            return Optional.empty();
        }
    }

    private Optional<? extends Node> modifyFunctionAccess(Node node) {
        if (node.is("function-access")) {
            return Optional.of(node.retype("data-access"));
        } else {
            return Optional.empty();
        }
    }

    private Optional<Node> modifyInvocation(Node node) {
        if (!node.is("invocation-statement") && !node.is("invocation-value")) return Optional.empty();

        final var caller = node.findNode("caller").orElse(new Node());
        final var arguments = node.findNodeList("arguments").orElse(new ArrayList<>());

        if (!caller.is("data-access")) return Optional.of(node);

        final var object = caller.findNode("object").orElse(new Node());
        final var property = caller.findString("property").orElse("");

        if (object.is("symbol")) {
            return Optional.of(node);
        }

        final var name = createUniqueName("local");
        final var symbol = new Node("symbol")
                .withString("symbol-value", name);
        final var newArguments = new ArrayList<Node>();
        newArguments.add(symbol);
        newArguments.addAll(arguments);

        final var children = List.of(
                new Node("initialization")
                        .withNode("definition", new Node("definition")
                                .withString("name", name)
                                .withNode("type", new Node("symbol").withString("symbol-value", "auto"))
                        ).withNode("value", object),
                new Node("invocation-value")
                        .withNode("caller", new Node("data-access")
                                .withNode("object", symbol)
                                .withString("property", property))
                        .withNodeList("arguments", newArguments)
        );

        final var group = new Node("group").withNodeList("children", children);
        return Optional.of(new Node("block").withNode("value", group));
    }

    private String createUniqueName(String prefix) {
        counter++;
        return "__" + prefix + counter + "__";
    }

    @Override
    public Tuple<State, Node> afterPass(State state, Node node) {
        final var result = modifyStateless(node);
        return new Tuple<>(state, result);
    }
}
