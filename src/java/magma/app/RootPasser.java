package magma.app;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.CompileError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static magma.app.lang.CommonLang.CONTENT_CHILDREN;
import static magma.app.lang.CommonLang.FUNCTIONAL_PARAMS;
import static magma.app.lang.CommonLang.FUNCTIONAL_RETURN;
import static magma.app.lang.CommonLang.FUNCTIONAL_TYPE;
import static magma.app.lang.CommonLang.GENERIC_CHILDREN;
import static magma.app.lang.CommonLang.GENERIC_PARENT;
import static magma.app.lang.CommonLang.GENERIC_TYPE;
import static magma.app.lang.CommonLang.METHOD_DEFINITION;
import static magma.app.lang.CommonLang.METHOD_PARAMS;
import static magma.app.lang.CommonLang.METHOD_TYPE;
import static magma.app.lang.CommonLang.METHOD_VALUE;
import static magma.app.lang.CommonLang.TUPLE_CHILDREN;
import static magma.app.lang.CommonLang.TUPLE_TYPE;

public class RootPasser implements Passer {
    static Node replaceWithInvocation(Node node) {
        final var type = node.findString("type").orElse("");
        final var symbol = createSymbol(type);
        return node.retype("invocation")
                .withNode("caller", new MapNode("data-access")
                        .withNode("ref", symbol)
                        .withString("property", "new"));
    }

    static Node replaceWithFunctional(Node generic) {
        return tryReplaceWithFunctional(generic)
                .map(functional -> {
                    return new MapNode(TUPLE_TYPE).withNodeList(TUPLE_CHILDREN, List.of(
                            createAnyType(),
                            functional
                    ));
                })
                .orElse(generic);
    }

    private static Optional<Node> tryReplaceWithFunctional(Node generic) {
        final var parent = generic.findString(GENERIC_PARENT).orElse("");
        final var children = generic.findNodeList(GENERIC_CHILDREN).orElse(Collections.emptyList());

        if (parent.equals("Supplier")) {
            return Optional.of(new MapNode("functional")
                    .withNodeList("params", List.of(createAnyType()))
                    .withNode("return", children.get(0)));
        }

        if (parent.equals("Function")) {
            final var params = new ArrayList<Node>();
            params.add(createAnyType());
            params.add(children.get(0));

            return Optional.of(new MapNode("functional")
                    .withNodeList("params", params)
                    .withNode("return", children.get(1)));
        }

        if (parent.equals("BiFunction")) {
            final var params = new ArrayList<Node>();
            params.add(createAnyType());
            params.add(children.get(0));
            params.add(children.get(1));

            return Optional.of(new MapNode("functional")
                    .withNodeList("params", params)
                    .withNode("return", children.get(2)));
        }

        return Optional.empty();
    }

    static Node retypeToStruct(Node node, List<Node> parameters) {
        final var name = node.findString("name").orElse("");
        return node.retype("struct").mapNode("value", value -> {
            return value.mapNodeList(CONTENT_CHILDREN, children -> {
                final var method = createConstructor(parameters, name);

                final Node withParameters;
                if (parameters.isEmpty()) {
                    withParameters = method;
                } else {
                    withParameters = method.withNodeList("params", parameters);
                }

                final var children1 = new ArrayList<Node>(children);
                children1.add(withParameters);
                return children1;
            });
        });
    }

    private static Node createConstructor(List<Node> parameters, String name) {
        final var thisType = new MapNode("struct")
                .withString("value", name);

        final var thisRef = createSymbol("this");
        final var thisDef = new MapNode("definition")
                .withNode("type", thisType)
                .withString("name", "this");

        final var thisReturn = new MapNode("return").withNode("value", thisRef);

        final var constructorChildren = new ArrayList<Node>();
        constructorChildren.add(thisDef);
        constructorChildren.addAll(parameters.stream()
                .map(RootPasser::createAssignment)
                .toList());
        constructorChildren.add(thisReturn);

        final var propertyValue = new MapNode("block").withNodeList(CONTENT_CHILDREN, constructorChildren);

        final var propertyValue1 = new MapNode("definition")
                .withString("name", "new")
                .withNode("type", thisType);

        return new MapNode(METHOD_TYPE)
                .withNode(METHOD_DEFINITION, propertyValue1)
                .withNode(METHOD_VALUE, propertyValue);
    }

    private static Node createAssignment(Node parameter) {
        final var paramName = parameter.findString("name").orElse("");
        final var propertyValue = new MapNode("data-access")
                .withNode("ref", createSymbol("this"))
                .withString("property", paramName);

        return new MapNode("assignment")
                .withNode("destination", propertyValue)
                .withNode("source", createSymbol(paramName));
    }

    private static Node createSymbol(String value) {
        return new MapNode("symbol").withString("value", value);
    }

    static Predicate<Node> by(String type) {
        return node -> node.is(type);
    }

    private static Node replaceWithDefinition(Node node) {
        final var value = node.findNode(METHOD_VALUE);
        if (value.isEmpty()) {
            final var params = new ArrayList<Node>();
            params.add(createAnyType());
            params.addAll(node.findNodeList(METHOD_PARAMS)
                    .orElse(new ArrayList<>())
                    .stream()
                    .map(param -> param.findNode("type"))
                    .flatMap(Optional::stream)
                    .toList());

            return node.findNode(METHOD_DEFINITION).orElse(new MapNode()).mapNode("type", type -> {
                final var withType = new MapNode(FUNCTIONAL_TYPE)
                        .withNode(FUNCTIONAL_RETURN, type);

                if (params.isEmpty()) {
                    return withType;
                } else {
                    return withType.withNodeList(FUNCTIONAL_PARAMS, params);
                }
            });
        }

        return node;
    }

    private static Node passInterface(Node node) {
        final var tableType = new MapNode("struct").withString("value", "VTable");
        final var tableDefinition = new MapNode("definition")
                .withNode("type", tableType)
                .withString("name", "vtable");

        final var refType = new MapNode("generic")
                .withString(GENERIC_PARENT, "Box")
                .withNodeList(GENERIC_CHILDREN, List.of(createAnyType()));
        final var refDefinition = new MapNode("definition")
                .withString("name", "ref")
                .withNode("type", refType);

        final var node1 = node.mapNode("value", value -> {
            return value.mapNodeList(CONTENT_CHILDREN, children -> {
                final var table = new MapNode("struct")
                        .withString("name", "VTable")
                        .withNode("value", new MapNode("block").withNodeList(CONTENT_CHILDREN, children));

                return List.of(
                        table,
                        refDefinition,
                        tableDefinition
                );
            });
        });


        return retypeToStruct(node1, List.of(refDefinition, tableDefinition));
    }

    private static Node createAnyType() {
        return createSymbol("void*");
    }

    @Override
    public Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit) {
        return new Ok<>(unit.filterAndMapToValue(by("root"), Formatter::cleanupNamespaced)
                .or(() -> unit.filterAndMapToValue(by("class").or(by("record")), (Node node) -> retypeToStruct(node, Collections.emptyList())))
                .or(() -> unit.filterAndMapToValue(by("interface"), RootPasser::passInterface))
                .orElse(unit));
    }

    @Override
    public Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        return new Ok<>(unit.filterAndMapToValue(by(GENERIC_TYPE), RootPasser::replaceWithFunctional)
                .or(() -> unit.filterAndMapToValue(by("construction"), RootPasser::replaceWithInvocation))
                .or(() -> unit.filterAndMapToValue(by(METHOD_TYPE), RootPasser::replaceWithDefinition))
                .orElse(unit));
    }
}