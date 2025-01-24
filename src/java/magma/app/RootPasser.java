package magma.app;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.CompileError;

import java.util.ArrayList;
import java.util.List;

import static magma.app.lang.CommonLang.CONTENT_CHILDREN;
import static magma.app.lang.CommonLang.METHOD_VALUE;

public class RootPasser implements Passer {
    private static Node getStruct(Node node) {
        return node.retype("struct").mapNode("value", value -> {
            return value.mapNodeList(CONTENT_CHILDREN, children -> {
                final var copy = new ArrayList<Node>(children);
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

                final var impl = new MapNode("struct")
                        .withString("name", "Impl")
                        .withNode("value", block);

                copy.add(impl);
                return copy;
            });
        });
    }

    @Override
    public Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit) {
        return new Ok<>(unit);
    }

    @Override
    public Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        return new Ok<>(
                unit.filterAndMapToValue(Passer.by("class").or(Passer.by("record")), node -> node.retype("struct"))
                        .or(() -> unit.filterAndMapToValue(Passer.by("interface"), RootPasser::getStruct)).orElse(unit));
    }
}