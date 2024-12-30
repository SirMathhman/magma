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

    private static Node createAutoType() {
        return new Node("symbol").withString("symbol-value", "auto");
    }

    private static Node modifyLambdaParam(Node param) {
        final var name = param.findString("symbol-value").orElse("");
        return new Node("definition")
                .withString("name", name)
                .withNode("type", createAutoType());
    }

    private static Node createBlock(List<Node> children) {
        final var group = new Node("group").withNodeList("children", children);
        return new Node("block").withNode("value", group);
    }

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
                .or(() -> modifyLambda(node))
                .orElse(node);
    }

    private Optional<? extends Node> modifyLambda(Node node) {
        if (!node.is("lambda")) return Optional.empty();

        final var params = node.findNode("param")
                .stream()
                .map(Modifier::modifyLambdaParam)
                .toList();

        final var value = node.findNode("value").orElse(new Node());
        final var block = createBlock(List.of(new Node("return").withNode("value", value)));

        final var function = node.retype("function")
                .withString("name", createUniqueName("function"))
                .withNode("type", createAutoType())
                .withNodeList("params", params)
                .withNode("value", block);

        return Optional.of(function);
    }

    private Optional<? extends Node> modifyArray(Node node) {
        if (node.is("array")) {
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
                                .withNode("type", createAutoType())
                        ).withNode("value", object),
                new Node("invocation-value")
                        .withNode("caller", new Node("data-access")
                                .withNode("object", symbol)
                                .withString("property", property))
                        .withNodeList("arguments", newArguments)
        );

        return Optional.of(createBlock(children));
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
