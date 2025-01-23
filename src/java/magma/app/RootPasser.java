package magma.app;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.CompileError;
import magma.app.lang.CommonLang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class RootPasser implements Passer {
    static Node replaceWithInvocation(Node node) {
        final var type = node.findString("type").orElse("");
        final var symbol = new MapNode("symbol").withString("value", type);
        return node.retype("invocation")
                .withNode("caller", new MapNode("data-access")
                        .withNode("ref", symbol)
                        .withString("property", "new"));
    }

    static Node replaceWithFunctional(Node generic) {
        final var parent = generic.findString(CommonLang.GENERIC_PARENT).orElse("");
        final var children = generic.findNodeList(CommonLang.GENERIC_CHILDREN).orElse(Collections.emptyList());

        if (parent.equals("Supplier")) {
            return new MapNode("functional").withNode("return", children.get(0));
        }
        if (parent.equals("Function")) {
            return new MapNode("functional")
                    .withNodeList("params", List.of(children.get(0)))
                    .withNode("return", children.get(1));
        }
        return generic;
    }

    static Node retypeToStruct(Node node) {
        final var name = node.findString("name").orElse("");
        return node.retype("struct").mapNode("value", value -> {
            return value.mapNodeList(CommonLang.CONTENT_CHILDREN, children -> {
                final var thisType = new MapNode("struct")
                        .withString("value", name);
                final var children1 = new ArrayList<Node>(children);
                final var propertyValue = new MapNode("block").withNodeList(CommonLang.CONTENT_CHILDREN, List.of(
                        new MapNode("definition")
                                .withNode("type", thisType)
                                .withString("name", "this"),
                        new MapNode("return").withNode("value", new MapNode("symbol").withString("value", "this"))));
                children1.add(new MapNode(CommonLang.METHOD_TYPE)
                        .withNode(CommonLang.METHOD_DEFINITION, new MapNode("definition")
                                .withString("name", "new")
                                .withNode("type", thisType))
                        .withNode(CommonLang.METHOD_VALUE, propertyValue));
                return children1;
            });
        });
    }

    static Predicate<Node> by(String type) {
        return node -> node.is(type);
    }

    @Override
    public Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit) {
        return new Ok<>(unit.filterAndMapToValue(by("root"), Formatter::cleanupNamespaced)
                .or(() -> unit.filterAndMapToValue(by("class").or(by("record")).or(by("interface")), RootPasser::retypeToStruct))
                .orElse(unit));
    }

    @Override
    public Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        return new Ok<>(unit.filterAndMapToValue(by(CommonLang.GENERIC_TYPE), RootPasser::replaceWithFunctional)
                .or(() -> unit.filterAndMapToValue(by("construction"), RootPasser::replaceWithInvocation))
                .orElse(unit));
    }
}