package magma.app;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.CompileError;

import java.util.ArrayList;
import java.util.List;

import static magma.app.lang.CommonLang.CONTENT_CHILDREN;
import static magma.app.lang.CommonLang.GENERIC_CHILDREN;
import static magma.app.lang.CommonLang.GENERIC_CONSTRUCTOR;
import static magma.app.lang.CommonLang.METHOD_VALUE;

public class RootPasser implements Passer {
    private static Node unwrapInterface(Node node) {
        return convertToStruct(node).mapNode("value", value -> value.mapNodeList(CONTENT_CHILDREN, children -> {
            final var copy = new ArrayList<>(children);
            final var impl = createConstructor();
            copy.add(impl);
            return copy;
        }));
    }

    private static Node createConstructor() {
        final var thisType = new MapNode("struct").withString("value", "Impl");
        final var thisTypeDefinition = new MapNode("definition")
                .withNode("type", thisType);

        final var constructorDefinition = thisTypeDefinition
                .withString("name", "new");

        final var thisSymbol = new MapNode("symbol").withString("value", "this");
        final var constructorBody = new MapNode("block")
                .withNodeList("children", List.of(
                        thisTypeDefinition.withString("name", "this"),
                        new MapNode("return").withNode("value", thisSymbol)
                ));

        final var constructor = new MapNode("method")
                .withNode("definition", constructorDefinition)
                .withNode(METHOD_VALUE, constructorBody);

        final var block = new MapNode("block")
                .withNodeList(CONTENT_CHILDREN, List.of(constructor));

        return new MapNode("struct")
                .withString("name", "Impl")
                .withNode("value", block);
    }

    private static Node convertToStruct(Node node) {
        return node.retype("struct").mapNode("value", value -> value.mapNodeList("children", children -> {
            final var maybeSupertype = node.findNode("supertype");
            if (maybeSupertype.isPresent()) {
                final var supertype = maybeSupertype.get();
                final var converter = createConverter(supertype);
                final var copy = new ArrayList<>(children);
                copy.add(converter);
                return copy;
            }

            return children;
        })).removeNode("supertype");
    }

    private static Node createConverter(Node supertype) {
        final var name = supertype.findString("name")
                .or(() -> supertype.findString(GENERIC_CONSTRUCTOR))
                .orElse("N/A");

        final var converterDefinition = new MapNode("definition")
                .withNode("type", supertype)
                .withString("name", name);

        final var supertypeRef = new MapNode("symbol")
                .withString("value", name);

        final var caller = new MapNode("data-access")
                .withNode("ref", supertypeRef)
                .withString("property", "new");

        final var returnsValue = new MapNode("invocation").withNode("caller", caller);
        final var returns = new MapNode("return").withNode("value", returnsValue);

        final var converterBody = new MapNode("block").withNodeList("children", List.of(returns));

        return new MapNode("method")
                .withNode("definition", converterDefinition)
                .withNode("value", converterBody);
    }

    private static Node mapToFunctional(Node node) {
        final var optional = node.findString(GENERIC_CONSTRUCTOR);
        if (optional.isPresent()) {
            final var constructor = optional.get();
            final var children = node.findNodeList(GENERIC_CHILDREN).orElse(new ArrayList<>());

            if (constructor.equals("Supplier")) {
                final var returns = children.get(0);
                return new MapNode("functional").withNode("return", returns);
            }

            if (constructor.equals("Function")) {
                final var param = children.get(0);
                final var returns = children.get(1);
                return new MapNode("functional")
                        .withNodeList("params", List.of(param))
                        .withNode("return", returns);
            }
        }
        return node;
    }

    @Override
    public Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit) {
        return new Ok<>(unit);
    }

    @Override
    public Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        return new Ok<>(unit.filterAndMapToValue(Passer.by("class").or(Passer.by("record")), RootPasser::convertToStruct)
                .or(() -> unit.filterAndMapToValue(Passer.by("interface"), RootPasser::unwrapInterface))
                .or(() -> unit.filterAndMapToValue(Passer.by("generic"), RootPasser::mapToFunctional))
                .orElse(unit));
    }
}